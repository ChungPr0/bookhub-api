package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryTreeResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.AdminCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private AdminCategoryServiceImpl adminCategoryService;

    private Category rootCategory;
    private Category childCategory;
    private Category subChildCategory;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
                .id(1L)
                .name("Sách Trong Nước")
                .slug("sach-trong-nuoc")
                .sortOrder(1)
                .children(new ArrayList<>())
                .build();

        childCategory = Category.builder()
                .id(2L)
                .name("Văn Học")
                .slug("van-hoc")
                .sortOrder(1)
                .parent(rootCategory)
                .children(new ArrayList<>())
                .build();

        subChildCategory = Category.builder()
                .id(3L)
                .name("Tiểu Thuyết")
                .slug("tieu-thuyet")
                .sortOrder(1)
                .parent(childCategory)
                .children(new ArrayList<>())
                .build();

        rootCategory.getChildren().add(childCategory);
        childCategory.getChildren().add(subChildCategory);
    }

    @Test
    @DisplayName("getAdminCategoryTree - Thành công trả về cây danh mục đa cấp và thống kê sách")
    void testGetAdminCategoryTree_Success() {
        when(categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc()).thenReturn(List.of(rootCategory));
        when(categoryRepository.countTotalBooksByCategoryId(1L)).thenReturn(0L);
        when(categoryRepository.countTotalBooksByCategoryIdRecursive(1L)).thenReturn(10L);
        when(categoryRepository.countTotalBooksByCategoryId(2L)).thenReturn(4L);
        when(categoryRepository.countTotalBooksByCategoryIdRecursive(2L)).thenReturn(10L);
        when(categoryRepository.countTotalBooksByCategoryId(3L)).thenReturn(6L);
        when(categoryRepository.countTotalBooksByCategoryIdRecursive(3L)).thenReturn(6L);

        List<AdminCategoryTreeResponse> tree = adminCategoryService.getAdminCategoryTree();

        assertThat(tree).hasSize(1);
        AdminCategoryTreeResponse rootNode = tree.get(0);
        assertThat(rootNode.getName()).isEqualTo("Sách Trong Nước");
        assertThat(rootNode.getDepth()).isEqualTo(1);
        assertThat(rootNode.getTotalBookCount()).isEqualTo(10L);
        assertThat(rootNode.getChildren()).hasSize(1);
        assertThat(rootNode.getChildren().get(0).getName()).isEqualTo("Văn Học");
        assertThat(rootNode.getChildren().get(0).getDepth()).isEqualTo(2);
    }

    @Test
    @DisplayName("createCategory - Thành công tạo danh mục gốc")
    void testCreateCategory_RootSuccess() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Kinh Tế")
                .sortOrder(2)
                .build();

        when(categoryRepository.existsByParentIdAndName(null, "Kinh Tế")).thenReturn(false);
        when(categoryRepository.existsBySlug("kinh-te")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(10L);
            return cat;
        });

        AdminCategoryResponse response = adminCategoryService.createCategory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Kinh Tế");
        assertThat(response.getSlug()).isEqualTo("kinh-te");
        assertThat(response.getParentId()).isNull();
    }

    @Test
    @DisplayName("createCategory - Ném lỗi CATEGORY_NAME_DUPLICATE khi tên đã tồn tại trong cùng danh mục cha")
    void testCreateCategory_DuplicateName() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Văn Học")
                .parentId(1L)
                .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.existsByParentIdAndName(1L, "Văn Học")).thenReturn(true);

        assertThatThrownBy(() -> adminCategoryService.createCategory(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NAME_DUPLICATE));
    }

    @Test
    @DisplayName("createCategory - Ném lỗi CATEGORY_MAX_DEPTH_EXCEEDED khi vượt quá 3 cấp")
    void testCreateCategory_MaxDepthExceeded() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Tiểu Thuyết Trinh Thám")
                .parentId(3L)
                .build();

        when(categoryRepository.findById(3L)).thenReturn(Optional.of(subChildCategory));

        assertThatThrownBy(() -> adminCategoryService.createCategory(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.CATEGORY_MAX_DEPTH_EXCEEDED));
    }

    @Test
    @DisplayName("updateCategory - Thành công cập nhật danh mục")
    void testUpdateCategory_Success() {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Văn Học Cổ Điển")
                .sortOrder(5)
                .parentId(1L)
                .build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.existsByParentIdAndNameAndIdNot(1L, "Văn Học Cổ Điển", 2L)).thenReturn(false);
        when(categoryRepository.existsBySlugAndIdNot("van-hoc-co-dien", 2L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(childCategory);

        AdminCategoryResponse response = adminCategoryService.updateCategory(2L, request);

        assertThat(response).isNotNull();
        assertThat(childCategory.getName()).isEqualTo("Văn Học Cổ Điển");
        assertThat(childCategory.getSlug()).isEqualTo("van-hoc-co-dien");
        assertThat(childCategory.getSortOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("updateCategory - Ném lỗi CATEGORY_CIRCULAR_REFERENCE khi chọn chính nó hoặc con cháu làm cha")
    void testUpdateCategory_CircularReference() {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Văn Học")
                .parentId(2L)
                .build();

        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));

        assertThatThrownBy(() -> adminCategoryService.updateCategory(2L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.CATEGORY_CIRCULAR_REFERENCE));
    }

    @Test
    @DisplayName("deleteCategory - Ném lỗi CATEGORY_HAS_CHILDREN khi danh mục còn danh mục con")
    void testDeleteCategory_HasChildren() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.existsByParentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> adminCategoryService.deleteCategory(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.CATEGORY_HAS_CHILDREN));
    }

    @Test
    @DisplayName("deleteCategory - Ném lỗi CATEGORY_HAS_BOOKS khi danh mục còn sách trực thuộc")
    void testDeleteCategory_HasBooks() {
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(subChildCategory));
        when(categoryRepository.existsByParentId(3L)).thenReturn(false);
        when(categoryRepository.countTotalBooksByCategoryId(3L)).thenReturn(5L);

        assertThatThrownBy(() -> adminCategoryService.deleteCategory(3L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.CATEGORY_HAS_BOOKS));
    }

    @Test
    @DisplayName("deleteCategory - Thành công xóa danh mục khi không có con và không có sách")
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(subChildCategory));
        when(categoryRepository.existsByParentId(3L)).thenReturn(false);
        when(categoryRepository.countTotalBooksByCategoryId(3L)).thenReturn(0L);

        adminCategoryService.deleteCategory(3L);

        verify(categoryRepository).delete(subChildCategory);
    }
}

