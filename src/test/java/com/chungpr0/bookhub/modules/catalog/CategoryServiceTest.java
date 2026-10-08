package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryTreeResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.CategoryServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        rootCategory = Category.builder()
                .id(1L)
                .name("Văn học")
                .slug("van-hoc")
                .sortOrder(0)
                .children(new ArrayList<>())
                .build();

        childCategory = Category.builder()
                .id(2L)
                .parent(rootCategory)
                .name("Tiểu thuyết")
                .slug("tieu-thuyet")
                .sortOrder(1)
                .children(new ArrayList<>())
                .build();

        rootCategory.getChildren().add(childCategory);
    }

    @Test
    @DisplayName("getCategoryTree - Thành công trả về cây danh mục")
    void testGetCategoryTree_Success() {
        when(categoryRepository.findByParentIsNullOrderBySortOrderAscNameAsc()).thenReturn(List.of(rootCategory));
        when(categoryRepository.countActiveBooksByCategoryIdRecursive(anyLong())).thenReturn(10);

        List<CategoryTreeResponse> tree = categoryService.getCategoryTree();

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getName()).isEqualTo("Văn học");
        assertThat(tree.get(0).getChildren()).hasSize(1);
        assertThat(tree.get(0).getChildren().get(0).getName()).isEqualTo("Tiểu thuyết");
    }

    @Test
    @DisplayName("getCategoryBySlug - Thành công trả về chi tiết kèm breadcrumb")
    void testGetCategoryBySlug_Success() {
        when(categoryRepository.findBySlug("tieu-thuyet")).thenReturn(Optional.of(childCategory));
        when(categoryRepository.countActiveBooksByCategoryIdRecursive(anyLong())).thenReturn(5);

        CategoryDetailResponse detail = categoryService.getCategoryBySlug("tieu-thuyet");

        assertThat(detail).isNotNull();
        assertThat(detail.getName()).isEqualTo("Tiểu thuyết");
        assertThat(detail.getBreadcrumb()).hasSize(2);
        assertThat(detail.getBreadcrumb().get(0).getSlug()).isEqualTo("van-hoc");
        assertThat(detail.getBreadcrumb().get(1).getSlug()).isEqualTo("tieu-thuyet");
    }

    @Test
    @DisplayName("getCategoryBySlug - Ném lỗi CATEGORY_NOT_FOUND khi slug không tồn tại")
    void testGetCategoryBySlug_NotFound() {
        when(categoryRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryBySlug("unknown"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
                });
    }
}

