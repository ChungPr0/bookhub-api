package com.chungpr0.bookhub.modules.order.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.order.dto.request.CalculateShippingRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateShippingConfigRequest;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingCalculateResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingConfigResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingServiceResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingTrackingResponse;
import com.chungpr0.bookhub.modules.order.service.ShippingService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "5. Tính phí Vận chuyển & Giao nhận (Shipping)", description = "APIs tính cước phí vận chuyển, tra cứu hành trình bưu kiện và cấu hình cước phí của nhà sách")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/shipping/services")
    @Operation(
            summary = "SHP-01: Danh sách các gói giao hàng khả dụng",
            description = "Trả về danh sách các gói dịch vụ vận chuyển mà BookHub đang hợp tác (Giao tiêu chuẩn, Giao nhanh, Hỏa tốc 4 giờ)."
    )
    public ResponseEntity<ApiResponse<List<ShippingServiceResponse>>> getShippingServices() {
        List<ShippingServiceResponse> response = shippingService.getShippingServices();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dịch vụ giao hàng thành công", response));
    }

    @PostMapping("/shipping/calculate")
    @Operation(
            summary = "SHP-02: Tính phí vận chuyển & ước tính thời gian giao",
            description = "Tính toán cước phí thực tế dựa trên địa chỉ giao hàng, cân nặng các cuốn sách trong đơn và chính sách miễn phí vận chuyển."
    )
    public ResponseEntity<ApiResponse<ShippingCalculateResponse>> calculateShipping(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CalculateShippingRequest request
    ) {
        Long accountId = principal != null ? principal.getAccountId() : null;
        ShippingCalculateResponse response = shippingService.calculateShipping(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("Tính phí vận chuyển thành công", response));
    }

    @GetMapping("/shipping/tracking/{orderCode}")
    @Operation(
            summary = "SHP-03: Tra cứu hành trình bưu kiện đơn hàng",
            description = "Theo dõi chi tiết các mốc trạng thái vận chuyển của bưu kiện đơn hàng theo thời gian thực."
    )
    public ResponseEntity<ApiResponse<ShippingTrackingResponse>> getTracking(
            @Parameter(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM") @PathVariable String orderCode
    ) {
        ShippingTrackingResponse response = shippingService.getTracking(orderCode);
        return ResponseEntity.ok(ApiResponse.success("Tra cứu hành trình vận chuyển thành công", response));
    }

    @GetMapping("/admin/shipping/configs")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "SHP-04: Xem cấu hình cước phí & chính sách Freeship",
            description = "Quản trị viên xem bảng cước phí tiêu chuẩn, hỏa tốc, ngưỡng miễn phí ship và phụ phí vượt cân nặng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<ShippingConfigResponse>> getShippingConfig() {
        ShippingConfigResponse response = shippingService.getShippingConfig();
        return ResponseEntity.ok(ApiResponse.success("Lấy cấu hình cước phí vận chuyển thành công", response));
    }

    @PutMapping("/admin/shipping/configs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "SHP-05: Cập nhật cấu hình cước phí vận chuyển",
            description = "Chỉ ADMIN mới có quyền thay đổi bảng cước phí, ngưỡng freeship và định mức cân nặng kiện hàng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<ShippingConfigResponse>> updateShippingConfig(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateShippingConfigRequest request
    ) {
        ShippingConfigResponse response = shippingService.updateShippingConfig(principal.getAccountId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật cấu hình cước phí vận chuyển thành công", response));
    }
}

