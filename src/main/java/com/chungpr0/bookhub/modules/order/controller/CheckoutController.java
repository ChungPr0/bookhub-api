package com.chungpr0.bookhub.modules.order.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.order.dto.request.CheckoutPreviewRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AvailableVouchersResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CheckoutPreviewResponse;
import com.chungpr0.bookhub.modules.order.dto.response.PaymentMethodResponse;
import com.chungpr0.bookhub.modules.order.service.OrderService;
import com.chungpr0.bookhub.modules.order.service.PaymentService;
import com.chungpr0.bookhub.modules.order.service.VoucherService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "5. Khởi tạo Đơn hàng & Thanh toán (Checkout)", description = "APIs hỗ trợ khởi tạo đơn hàng, xem trước giá tiền, phương thức thanh toán và mã voucher khả dụng")
public class CheckoutController {

    private final PaymentService paymentService;
    private final VoucherService voucherService;
    private final OrderService orderService;

    @GetMapping("/payment-methods")
    @Operation(
            summary = "CHK-01: Lấy danh sách phương thức thanh toán khả dụng",
            description = "Trả về toàn bộ các phương thức thanh toán đang mở kinh doanh (kèm thông tin số tài khoản ngân hàng nếu chọn Chuyển khoản)."
    )
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods() {
        List<PaymentMethodResponse> response = paymentService.getActivePaymentMethods();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phương thức thanh toán thành công", response));
    }

    @GetMapping("/me/vouchers/available")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "CHK-02: Lấy danh sách mã giảm giá khả dụng theo giỏ hàng",
            description = "Phân loại mã giảm giá thành 2 nhóm: nhóm có thể áp dụng ngay cho đơn hàng và nhóm chưa đủ điều kiện kèm lý do chi tiết.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<AvailableVouchersResponse>> getAvailableVouchers(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Tổng tiền hàng tạm tính của các cuốn sách đã chọn", example = "250000")
            @RequestParam(required = false) Long subtotal
    ) {
        AvailableVouchersResponse response = voucherService.getAvailableVouchers(principal.getAccountId(), subtotal);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách voucher thành công", response));
    }

    @PostMapping("/me/checkout/preview")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "CHK-03: Xem trước bảng tính tiền đơn hàng (Preview)",
            description = "Tính toán chi tiết tiền hàng, chiết khấu voucher, giảm giá điểm thưởng, phí vận chuyển và tiến trình miễn phí ship mà không ghi CSDL hay giữ hàng tồn kho.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CheckoutPreviewResponse>> previewCheckout(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CheckoutPreviewRequest request
    ) {
        CheckoutPreviewResponse response = orderService.previewCheckout(principal.getAccountId(), request);
        return ResponseEntity.ok(ApiResponse.success("Tính toán tiền đơn hàng thành công", response));
    }
}

