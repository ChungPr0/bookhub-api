package com.chungpr0.bookhub.modules.order.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.order.dto.request.CreateOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CustomerCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.RecreatePaymentUrlRequest;
import com.chungpr0.bookhub.modules.order.dto.response.CreateOrderResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderStatusCountResponse;
import com.chungpr0.bookhub.modules.order.dto.response.RecreatePaymentUrlResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ReorderReportResponse;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.service.OrderService;
import com.chungpr0.bookhub.modules.order.service.PaymentService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/me/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "5. Đơn hàng của tôi (Customer Orders)", description = "APIs quản lý vòng đời đơn hàng cá nhân của khách hàng: tạo đơn, tra cứu, hủy, xác nhận nhận hàng, mua lại")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "CHK-04: Tạo đơn hàng mới (Chốt thanh toán)",
            description = "Tạo đơn hàng chính thức với cơ chế chống trượt giá (expectedFinalAmount) và phòng chống đặt trùng lặp qua header Idempotency-Key. Trừ tồn kho, lưu snapshot, trừ điểm và xóa giỏ.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Khóa chống trùng lặp tạo đơn (UUID v4)", example = "d9b2d354-933e-4b72-97ec-03a116dc21f7")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        CreateOrderResponse response = orderService.createOrder(principal.getAccountId(), idempotencyKey, request, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đặt hàng thành công", response));
    }

    @GetMapping
    @Operation(
            summary = "ORD-01: Lấy danh sách đơn hàng cá nhân",
            description = "Trả về danh sách đơn hàng của khách hàng với phân trang và lọc theo trạng thái, từ khóa mã đơn / tên sách.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<CustomerOrderSummaryResponse>>> getCustomerOrders(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Trạng thái đơn hàng cần lọc") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Từ khóa tìm kiếm mã đơn hoặc tên sách", example = "ORD-20261007") @RequestParam(required = false) String keyword,
            @Parameter(description = "Từ ngày đặt") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @Parameter(description = "Đến ngày đặt") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<CustomerOrderSummaryResponse> response = orderService.getCustomerOrders(
                principal.getAccountId(),
                status,
                keyword,
                createdFrom,
                createdTo,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", response));
    }

    @GetMapping("/status-counts")
    @Operation(
            summary = "ORD-02: Thống kê số lượng đơn hàng theo từng trạng thái (Badge tab)",
            description = "Trả về số lượng đơn hàng theo từng nhóm trạng thái để hiển thị số lượng trên các tab danh mục đơn hàng của khách hàng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<OrderStatusCountResponse>> getCustomerStatusCounts(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        OrderStatusCountResponse response = orderService.getCustomerStatusCounts(principal.getAccountId());
        return ResponseEntity.ok(ApiResponse.success("Lấy số lượng đơn hàng thành công", response));
    }

    @GetMapping("/{orderCode}")
    @Operation(
            summary = "ORD-03: Xem chi tiết đơn hàng cá nhân",
            description = "Xem toàn bộ thông tin chi tiết đơn hàng (snapshot sách, người nhận, timeline tiến trình, và danh mục hành động được phép allowedActions).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CustomerOrderDetailResponse>> getCustomerOrderDetail(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM") @PathVariable String orderCode
    ) {
        CustomerOrderDetailResponse response = orderService.getCustomerOrderDetail(principal.getAccountId(), orderCode);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", response));
    }

    @PostMapping("/{orderCode}/cancel")
    @Operation(
            summary = "ORD-04: Khách hàng tự hủy đơn hàng",
            description = "Khách hàng tự hủy đơn hàng khi đơn còn ở trạng thái PENDING. Tự động hoàn lại tồn kho, trả lại lượt voucher và hoàn lại điểm thưởng đã dùng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CustomerOrderDetailResponse>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM") @PathVariable String orderCode,
            @Valid @RequestBody CustomerCancelOrderRequest request
    ) {
        CustomerOrderDetailResponse response = orderService.cancelOrder(principal.getAccountId(), orderCode, request);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", response));
    }

    @PostMapping("/{orderCode}/confirm-received")
    @Operation(
            summary = "ORD-05: Xác nhận đã nhận được hàng",
            description = "Khách hàng bấm xác nhận khi đơn đang ở trạng thái giao hàng SHIPPING. Hệ thống chuyển sang COMPLETED, tích điểm thưởng và cập nhật chi tiêu.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CustomerOrderDetailResponse>> confirmReceived(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM") @PathVariable String orderCode
    ) {
        CustomerOrderDetailResponse response = orderService.confirmReceived(principal.getAccountId(), orderCode);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận nhận hàng thành công", response));
    }

    @PostMapping("/{orderCode}/reorder")
    @Operation(
            summary = "ORD-06: Mua lại đơn hàng cũ (Reorder)",
            description = "Tự động lấy toàn bộ các cuốn sách còn hàng trong đơn hàng cũ thêm vào Giỏ hàng hiện tại của khách hàng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<ReorderReportResponse>> reorder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM") @PathVariable String orderCode
    ) {
        ReorderReportResponse response = orderService.reorder(principal.getAccountId(), orderCode);
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm từ đơn hàng cũ vào giỏ thành công", response));
    }

    @PostMapping("/{orderCode}/payments")
    @Operation(
            summary = "PAY-01: Tạo lại đường link thanh toán VNPAY",
            description = "Được gọi khi khách hàng thanh toán online thất bại hoặc lỡ đóng tab và muốn thanh toán lại khi đơn còn trong hạn 15 phút.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<RecreatePaymentUrlResponse>> recreatePaymentUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM") @PathVariable String orderCode,
            @Valid @RequestBody(required = false) RecreatePaymentUrlRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        RecreatePaymentUrlResponse response = paymentService.recreatePaymentUrl(
                principal.getAccountId(),
                orderCode,
                request,
                clientIp
        );
        return ResponseEntity.ok(ApiResponse.success("Tạo đường dẫn thanh toán thành công", response));
    }
}

