package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CategoryTreeResponse;
import com.chungpr0.bookhub.modules.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "3.1 Danh mục Công khai (Categories)", description = "APIs truy vấn cây danh mục và thông tin chi tiết danh mục công khai")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    @Operation(
            summary = "CAT-01: Lấy cây danh mục Menu Header",
            description = "Trả về cấu trúc cây danh mục đệ quy tối đa 3 cấp kèm theo số lượng sách đang mở bán (bookCount) phục vụ hiển thị menu điều hướng."
    )
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        List<CategoryTreeResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(ApiResponse.success("Lấy cây danh mục thành công", tree));
    }

    @GetMapping("/{slug}")
    @Operation(
            summary = "CAT-02: Chi tiết danh mục & Breadcrumb",
            description = "Lấy thông tin chi tiết một danh mục theo slug, bao gồm chuỗi breadcrumb từ gốc và danh sách các danh mục con trực tiếp."
    )
    public ResponseEntity<ApiResponse<CategoryDetailResponse>> getCategoryBySlug(
            @Parameter(description = "Đường dẫn thân thiện (slug) của danh mục", example = "tieu-thuyet", required = true)
            @PathVariable String slug
    ) {
        CategoryDetailResponse detail = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin danh mục thành công", detail));
    }
}

