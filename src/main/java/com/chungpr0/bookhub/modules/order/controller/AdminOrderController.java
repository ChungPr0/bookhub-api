package com.chungpr0.bookhub.modules.order.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.order.dto.request.AdminBankTransferConfirmRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminRefundOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminReturnOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminUpdateOrderStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.OrderFilterRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderStatusHistoryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderStatusCountResponse;
import com.chungpr0.bookhub.modules.order.service.AdminOrderService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
@Tag(name = "5. Quản lý Đơn hàng (Admin)", description = "APIs vận hành, duyệt đơn, chuyển trạng thái, hủy đơn, xác nhận chuyển khoản và hoàn tiền của nhà sách")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    @Operation(
            summary = "AOR-01: Tìm kiếm & lọc danh sách đơn hàng Admin",
            description = "Hỗ trợ tìm kiếm theo từ khóa (mã đơn, tên người nhận, SĐT), trạng thái vận đơn, thanh toán, ngày tạo và khoảng tiền bằng JPA Specification.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<AdminOrderSummaryResponse>>> searchOrders(
            @ModelAttribute OrderFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<AdminOrderSummaryResponse> response = adminOrderService.searchOrders(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công", response));
    }

    @GetMapping("/status-counts")
    @Operation(
            summary = "AOR-02: Thống kê số lượng đơn theo trạng thái (kèm cảnh báo cần xử lý)",
            description = "Trả về tổng số đơn theo từng trạng thái và các mục cần chú ý (đơn chuyển khoản chờ duyệt tiền, đơn chờ hoàn tiền).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<OrderStatusCountResponse>> getStatusCounts() {
        OrderStatusCountResponse response = adminOrderService.getAdminStatusCounts();
        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê số lượng đơn thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "AOR-03: Chi tiết đơn hàng Admin (Xem giá vốn & lợi nhuận)",
            description = "Hiển thị đầy đủ thông tin đơn hàng, thông tin khách hàng, các dòng sách kèm giá vốn FIFO và lợi nhuận gộp, lịch sử thanh toán.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> getOrderDetail(
            @Parameter(description = "ID đơn hàng", example = "8801") @PathVariable Long id
    ) {
        AdminOrderDetailResponse response = adminOrderService.getAdminOrderDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "AOR-04: Cập nhật trạng thái xử lý đơn hàng",
            description = "Chuyển trạng thái đơn hàng theo luồng PENDING -> CONFIRMED -> SHIPPING -> COMPLETED. Kiểm tra Optimistic Lock và điều kiện đơn online phải thanh toán trước khi giao hàng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> updateOrderStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID đơn hàng", example = "8801") @PathVariable Long id,
            @Valid @RequestBody AdminUpdateOrderStatusRequest request
    ) {
        AdminOrderDetailResponse response = adminOrderService.updateOrderStatus(principal.getAccountId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "AOR-05: Cửa hàng chủ động hủy đơn hàng",
            description = "Nhân viên hủy đơn do hết hàng, giao thất bại hoặc nghi ngờ gian lận. Tự động hoàn kho, hoàn voucher, hoàn điểm và chuyển sang chờ hoàn tiền nếu đã thanh toán.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> cancelOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID đơn hàng", example = "8801") @PathVariable Long id,
            @Valid @RequestBody AdminCancelOrderRequest request
    ) {
        AdminOrderDetailResponse response = adminOrderService.cancelOrder(principal.getAccountId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công", response));
    }

    @PostMapping("/{id}/payment-confirmation")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AOR-06: Xác nhận nhận tiền chuyển khoản ngân hàng",
            description = "Kế toán kiểm tra và xác nhận đã nhận đủ tiền chuyển khoản khớp với giá trị đơn hàng BANK_TRANSFER. Chuyển đơn sang PAID và tự động CONFIRMED.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> confirmPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID đơn hàng", example = "8801") @PathVariable Long id,
            @Valid @RequestBody AdminBankTransferConfirmRequest request
    ) {
        AdminOrderDetailResponse response = adminOrderService.confirmBankTransferPayment(principal.getAccountId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận thanh toán thành công", response));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AOR-07: Tiếp nhận hoàn trả đơn hàng (trong vòng 7 ngày)",
            description = "Tiếp nhận trả hàng cho đơn đã COMPLETED trong 7 ngày. Thu hồi điểm thưởng đã tích, giảm chi tiêu và tùy chọn nhập lại sách vào kho.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> returnOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID đơn hàng", example = "8801") @PathVariable Long id,
            @Valid @RequestBody AdminReturnOrderRequest request
    ) {
        AdminOrderDetailResponse response = adminOrderService.returnOrder(principal.getAccountId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Tiếp nhận hoàn trả đơn hàng thành công", response));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "AOR-08: Đánh dấu đã hoàn tiền cho khách hàng",
            description = "Kế toán đánh dấu đã hoàn tiền thành công vào tài khoản khách đối với đơn hủy hoặc đơn hoàn trả đang ở trạng thái REFUND_PENDING.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<AdminOrderDetailResponse>> refundOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "ID đơn hàng", example = "8801") @PathVariable Long id,
            @Parameter(description = "Khóa chống trùng lặp hoàn tiền (UUID v4)")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody AdminRefundOrderRequest request
    ) {
        AdminOrderDetailResponse response = adminOrderService.refundOrder(principal.getAccountId(), id, idempotencyKey, request);
        return ResponseEntity.ok(ApiResponse.success("Ghi nhận hoàn tiền thành công", response));
    }

    @GetMapping("/{id}/history")
    @Operation(
            summary = "AOR-09: Lịch sử thay đổi trạng thái đơn hàng",
            description = "Tra cứu toàn bộ lịch sử các lần chuyển trạng thái đơn hàng cùng nhân viên thực hiện thao tác.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<List<AdminOrderStatusHistoryResponse>>> getOrderHistory(
            @Parameter(description = "ID đơn hàng", example = "8801") @PathVariable Long id
    ) {
        List<AdminOrderStatusHistoryResponse> response = adminOrderService.getOrderStatusHistories(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử trạng thái thành công", response));
    }
}

