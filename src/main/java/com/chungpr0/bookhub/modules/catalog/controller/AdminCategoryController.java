package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminCategoryTreeResponse;
import com.chungpr0.bookhub.modules.catalog.service.AdminCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "6.1 Quản trị Danh mục (Admin Categories)", description = "APIs quản lý cây danh mục đa cấp dành cho Ban quản trị")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping("/tree")
    @Operation(
            summary = "ACT-01: Lấy cây danh mục Admin",
            description = "Hiển thị danh sách danh mục dạng cấu trúc cây lồng nhau (Treeview) kèm số lượng sách trực tiếp và tổng số sách."
    )
    public ResponseEntity<ApiResponse<List<AdminCategoryTreeResponse>>> getAdminCategoryTree() {
        List<AdminCategoryTreeResponse> tree = adminCategoryService.getAdminCategoryTree();
        return ResponseEntity.ok(ApiResponse.success("Lấy cây danh mục thành công", tree));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ACT-02: Thêm mới danh mục",
            description = "Tạo mới một danh mục sách (hỗ trợ tối đa 3 cấp, kiểm tra trùng tên trong cùng cấp cha)."
    )
    public ResponseEntity<ApiResponse<AdminCategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        AdminCategoryResponse response = adminCategoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm danh mục thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ACT-03: Cập nhật danh mục",
            description = "Cập nhật tên, danh mục cha, mô tả hoặc thứ tự sắp xếp của danh mục (chống tham chiếu vòng lặp và vượt quá 3 cấp)."
    )
    public ResponseEntity<ApiResponse<AdminCategoryResponse>> updateCategory(
            @Parameter(description = "ID danh mục", example = "8", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        AdminCategoryResponse response = adminCategoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ACT-04: Xóa danh mục",
            description = "Xóa một danh mục khỏi hệ thống. Bị chặn nếu danh mục còn chứa danh mục con hoặc còn liên kết với sách."
    )
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "ID danh mục", example = "8", required = true)
            @PathVariable Long id
    ) {
        adminCategoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa danh mục thành công", null));
    }
}

