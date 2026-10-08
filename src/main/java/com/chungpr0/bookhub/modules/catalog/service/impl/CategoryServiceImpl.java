package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryBreadcrumbItem;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryChildResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryTreeResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc();
        return rootCategories.stream()
                .map(this::mapToTreeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDetailResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        int bookCount = categoryRepository.countActiveBooksByCategoryIdRecursive(category.getId());

        List<CategoryBreadcrumbItem> breadcrumb = buildBreadcrumb(category);

        List<CategoryChildResponse> children = category.getChildren().stream()
                .map(child -> CategoryChildResponse.builder()
                        .id(child.getId())
                        .name(child.getName())
                        .slug(child.getSlug())
                        .bookCount(categoryRepository.countActiveBooksByCategoryIdRecursive(child.getId()))
                        .build())
                .toList();

        return CategoryDetailResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .bookCount(bookCount)
                .breadcrumb(breadcrumb)
                .children(children)
                .build();
    }

    private CategoryTreeResponse mapToTreeResponse(Category category) {
        int bookCount = categoryRepository.countActiveBooksByCategoryIdRecursive(category.getId());
        List<CategoryTreeResponse> children = category.getChildren() != null
                ? category.getChildren().stream().map(this::mapToTreeResponse).toList()
                : Collections.emptyList();

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .bookCount(bookCount)
                .children(children)
                .build();
    }

    private List<CategoryBreadcrumbItem> buildBreadcrumb(Category category) {
        List<CategoryBreadcrumbItem> breadcrumb = new ArrayList<>();
        Category current = category;
        while (current != null) {
            breadcrumb.add(CategoryBreadcrumbItem.builder()
                    .id(current.getId())
                    .name(current.getName())
                    .slug(current.getSlug())
                    .build());
            current = current.getParent();
        }
        Collections.reverse(breadcrumb);
        return breadcrumb;
    }
}

