package com.chungpr0.bookhub.modules.order.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.order.dto.request.CreateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ToggleVoucherStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdatePaymentMethodRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.VoucherFilterRequest;
import com.chungpr0.bookhub.modules.order.dto.response.PaymentMethodResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherUsageResponse;
import com.chungpr0.bookhub.modules.order.service.PaymentService;
import com.chungpr0.bookhub.modules.order.service.VoucherService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "5. Quản trị Khuyến mãi & Phương thức thanh toán (Admin Vouchers)", description = "APIs quản lý mã giảm giá voucher và cấu hình phương thức thanh toán của nhà sách")
public class AdminVoucherController {

    private final VoucherService voucherService;
    private final PaymentService paymentService;

    @GetMapping("/vouchers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AVC-01: Tìm kiếm & phân trang Voucher",
            description = "Dùng cho trang danh sách mã giảm giá, hỗ trợ tìm kiếm gần đúng, lọc theo trạng thái, loại giảm giá và phân trang.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<VoucherResponse>>> searchVouchers(
            @ModelAttribute VoucherFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<VoucherResponse> response = voucherService.searchVouchers(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách mã giảm giá thành công", response));
    }

    @GetMapping("/vouchers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AVC-02: Xem chi tiết Voucher",
            description = "Xem chi tiết cấu hình và trạng thái tính toán của mã giảm giá.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(
            @Parameter(description = "ID voucher", example = "12") @PathVariable Long id
    ) {
        VoucherResponse response = voucherService.getVoucherById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết mã giảm giá thành công", response));
    }

    @PostMapping("/vouchers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AVC-03: Tạo Voucher mới",
            description = "Tạo mới mã giảm giá với các quy định hạn mức số lượng, loại giảm giá và khoảng thời gian áp dụng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(
            @Valid @RequestBody CreateVoucherRequest request
    ) {
        VoucherResponse response = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo mã giảm giá thành công", response));
    }

    @PutMapping("/vouchers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AVC-04: Cập nhật cấu hình Voucher",
            description = "Cập nhật voucher. Nếu voucher đã phát sinh giao dịch (usedCount > 0), các thuộc tính tài chính cốt lõi bị khóa không được phép chỉnh sửa.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @Parameter(description = "ID voucher", example = "12") @PathVariable Long id,
            @Valid @RequestBody UpdateVoucherRequest request
    ) {
        VoucherResponse response = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật mã giảm giá thành công", response));
    }

    @PatchMapping("/vouchers/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AVC-05: Đổi trạng thái kích hoạt Voucher",
            description = "Bật hoặc tắt trạng thái hoạt động của mã giảm giá (ACTIVE hoặc INACTIVE).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<VoucherResponse>> toggleVoucherStatus(
            @Parameter(description = "ID voucher", example = "12") @PathVariable Long id,
            @Valid @RequestBody ToggleVoucherStatusRequest request
    ) {
        VoucherResponse response = voucherService.toggleStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái voucher thành công", response));
    }

    @DeleteMapping("/vouchers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AVC-06: Xóa Voucher",
            description = "Xóa mã giảm giá. Không cho phép xóa nếu voucher đã được sử dụng trong ít nhất một đơn hàng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(
            @Parameter(description = "ID voucher", example = "12") @PathVariable Long id
    ) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa mã giảm giá thành công", null));
    }

    @GetMapping("/vouchers/{id}/usages")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AVC-07: Lịch sử sử dụng Voucher",
            description = "Xem danh sách các đơn hàng và khách hàng đã áp dụng mã giảm giá này.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<VoucherUsageResponse>>> getVoucherUsages(
            @Parameter(description = "ID voucher", example = "12") @PathVariable Long id,
            @PageableDefault(size = 20, sort = "usedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<VoucherUsageResponse> response = voucherService.getVoucherUsages(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử sử dụng voucher thành công", response));
    }

    @GetMapping("/payment-methods")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "APM-01: Danh sách tất cả phương thức thanh toán",
            description = "Xem toàn bộ các phương thức thanh toán đang mở hoặc đang tắt trong hệ thống.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getAllPaymentMethods() {
        List<PaymentMethodResponse> response = paymentService.getAllPaymentMethods();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phương thức thanh toán thành công", response));
    }

    @PatchMapping("/payment-methods/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "APM-02: Cập nhật / Bật tắt phương thức thanh toán",
            description = "Chỉ ADMIN mới có quyền bật/tắt hoặc chỉnh sửa thông tin tài khoản ngân hàng. Bảo vệ không cho phép tắt phương thức thanh toán duy nhất còn lại.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> updatePaymentMethod(
            @Parameter(description = "ID phương thức thanh toán", example = "2") @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentMethodRequest request
    ) {
        PaymentMethodResponse response = paymentService.updatePaymentMethod(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phương thức thanh toán thành công", response));
    }
}

