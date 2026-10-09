package com.chungpr0.bookhub.modules.inventory.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.SupplierFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.UpdateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierResponse;
import com.chungpr0.bookhub.modules.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "7.1 Quản lý Nhà cung cấp (Admin Suppliers)", description = "APIs quản lý danh bạ, tìm kiếm, thêm mới, cập nhật và xóa nhà cung cấp dành cho Quản trị viên")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
public class AdminSupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @Operation(
            summary = "SUP-01: Danh sách nhà cung cấp Admin",
            description = "Tra cứu, tìm kiếm theo tên, MST, SĐT và phân trang danh sách nhà cung cấp kèm thống kê số phiếu nhập và tổng chi phí nhập lũy kế."
    )
    public ResponseEntity<ApiResponse<PageResponse<SupplierResponse>>> getSuppliers(
            @ModelAttribute SupplierFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<SupplierResponse> response = supplierService.getSuppliers(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà cung cấp thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "SUP-02: Chi tiết nhà cung cấp Admin",
            description = "Lấy đầy đủ thông tin chi tiết nhà cung cấp, thông tin liên hệ và số liệu nhập hàng."
    )
    public ResponseEntity<ApiResponse<SupplierDetailResponse>> getSupplierDetail(
            @Parameter(description = "ID nhà cung cấp", example = "1", required = true)
            @PathVariable Long id
    ) {
        SupplierDetailResponse response = supplierService.getSupplierDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết nhà cung cấp thành công", response));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "SUP-03: Thêm mới nhà cung cấp",
            description = "Thêm mới đối tác nhà cung cấp vào hệ thống. Chặn nếu trùng tên nhà cung cấp."
    )
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request
    ) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Thêm nhà cung cấp thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "SUP-04: Cập nhật thông tin nhà cung cấp",
            description = "Cập nhật tên, thông tin liên hệ, địa chỉ và mã số thuế của nhà cung cấp."
    )
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
            @Parameter(description = "ID nhà cung cấp", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupplierRequest request
    ) {
        SupplierResponse response = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin nhà cung cấp thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "SUP-05: Xóa nhà cung cấp",
            description = "Xóa hồ sơ nhà cung cấp. Bị chặn nếu nhà cung cấp đã có phiếu nhập kho phát sinh trong hệ thống."
    )
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(
            @Parameter(description = "ID nhà cung cấp", example = "1", required = true)
            @PathVariable Long id
    ) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhà cung cấp thành công", null));
    }
}

