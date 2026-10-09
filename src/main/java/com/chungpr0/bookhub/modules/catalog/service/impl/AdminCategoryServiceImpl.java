package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.SlugUtils;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryTreeResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AdminCategoryTreeResponse> getAdminCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc();
        List<AdminCategoryTreeResponse> tree = new ArrayList<>();
        for (Category root : rootCategories) {
            tree.add(buildAdminTreeNode(root, 1));
        }
        return tree;
    }

    private AdminCategoryTreeResponse buildAdminTreeNode(Category category, int depth) {
        long directBookCount = categoryRepository.countTotalBooksByCategoryId(category.getId());
        long totalBookCount = categoryRepository.countTotalBooksByCategoryIdRecursive(category.getId());

        List<AdminCategoryTreeResponse> childResponses = new ArrayList<>();
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                childResponses.add(buildAdminTreeNode(child, depth + 1));
            }
        }

        return AdminCategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .sortOrder(category.getSortOrder())
                .depth(depth)
                .bookCount(directBookCount)
                .totalBookCount(totalBookCount)
                .children(childResponses)
                .build();
    }

    @Override
    @Transactional
    public AdminCategoryResponse createCategory(CreateCategoryRequest request) {
        Category parent = null;
        int depth = 1;

        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Không tìm thấy danh mục cha"));
            depth = calculateDepth(parent) + 1;
            if (depth > 3) {
                throw new AppException(ErrorCode.CATEGORY_MAX_DEPTH_EXCEEDED, "Hệ thống chỉ hỗ trợ tối đa 3 cấp danh mục");
            }
        }

        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByParentIdAndName(request.getParentId(), trimmedName)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_DUPLICATE, "Tên danh mục đã tồn tại trong nhóm này");
        }

        String slug = generateUniqueSlug(trimmedName, null);

        Category category = Category.builder()
                .name(trimmedName)
                .slug(slug)
                .parent(parent)
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AdminCategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Không tìm thấy danh mục"));

        Category newParent = null;
        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new AppException(ErrorCode.CATEGORY_CIRCULAR_REFERENCE, "Danh mục không thể chọn chính mình làm danh mục cha");
            }

            newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Không tìm thấy danh mục cha"));

            // Check circular reference by traversing up from newParent
            Category current = newParent;
            while (current != null) {
                if (Objects.equals(current.getId(), id)) {
                    throw new AppException(ErrorCode.CATEGORY_CIRCULAR_REFERENCE, "Danh mục cha không thể là danh mục con của chính nó");
                }
                current = current.getParent();
            }

            int newParentDepth = calculateDepth(newParent);
            int subtreeHeight = calculateSubtreeHeight(category);
            if (newParentDepth + subtreeHeight > 3) {
                throw new AppException(ErrorCode.CATEGORY_MAX_DEPTH_EXCEEDED, "Cấu trúc danh mục vượt quá giới hạn 3 cấp");
            }
        }

        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByParentIdAndNameAndIdNot(request.getParentId(), trimmedName, id)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_DUPLICATE, "Tên danh mục đã tồn tại trong nhóm này");
        }

        if (!category.getName().equals(trimmedName)) {
            category.setName(trimmedName);
            category.setSlug(generateUniqueSlug(trimmedName, id));
        }

        category.setParent(newParent);
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND, "Không tìm thấy danh mục"));

        if (categoryRepository.existsByParentId(id)) {
            throw new AppException(ErrorCode.CATEGORY_HAS_CHILDREN, "Không thể xóa: Vui lòng xóa hoặc di chuyển các danh mục con trước");
        }

        long bookCount = categoryRepository.countTotalBooksByCategoryId(id);
        if (bookCount > 0) {
            throw new AppException(ErrorCode.CATEGORY_HAS_BOOKS, "Không thể xóa: Danh mục này đang chứa " + bookCount + " cuốn sách");
        }

        categoryRepository.delete(category);
    }

    private int calculateDepth(Category category) {
        int depth = 1;
        Category current = category.getParent();
        while (current != null) {
            depth++;
            current = current.getParent();
        }
        return depth;
    }

    private int calculateSubtreeHeight(Category category) {
        if (category.getChildren() == null || category.getChildren().isEmpty()) {
            return 1;
        }
        int maxChildHeight = 0;
        for (Category child : category.getChildren()) {
            maxChildHeight = Math.max(maxChildHeight, calculateSubtreeHeight(child));
        }
        return 1 + maxChildHeight;
    }

    private String generateUniqueSlug(String name, Long currentId) {
        String baseSlug = SlugUtils.toSlug(name);
        String candidateSlug = baseSlug;
        int counter = 2;

        while (isSlugConflict(candidateSlug, currentId)) {
            candidateSlug = baseSlug + "-" + counter;
            counter++;
        }
        return candidateSlug;
    }

    private boolean isSlugConflict(String slug, Long currentId) {
        if (currentId == null) {
            return categoryRepository.existsBySlug(slug);
        }
        return categoryRepository.existsBySlugAndIdNot(slug, currentId);
    }

    private AdminCategoryResponse mapToResponse(Category category) {
        return AdminCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}

