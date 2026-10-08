package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderDetailResponse {

    @Schema(description = "ID đơn hàng", example = "8801")
    private Long id;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "Trạng thái vận đơn hiện tại", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Trạng thái thanh toán hiện tại", example = "UNPAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Phương thức thanh toán đã chọn")
    private PaymentMethodResponse paymentMethod;

    @Schema(description = "Thời hạn thanh toán đơn hàng (VNPAY: 15 phút)")
    private OffsetDateTime paymentExpiresAt;

    @Schema(description = "Thông tin người nhận hàng")
    private OrderReceiverResponse receiver;

    @Schema(description = "Danh sách sản phẩm trong đơn")
    @Builder.Default
    private List<OrderItemDetailResponse> items = new ArrayList<>();

    @Schema(description = "Bảng kê khai chi tiết giá cả và tiền thanh toán")
    private OrderPricingResponse pricing;

    @Schema(description = "Danh sách hành động khách hàng được phép thao tác (CANCEL, PAY, CONFIRM_RECEIVED, REORDER)", example = "[\"CANCEL\", \"PAY\"]")
    @Builder.Default
    private List<String> allowedActions = new ArrayList<>();

    @Schema(description = "Tiến trình thời gian xử lý đơn hàng")
    @Builder.Default
    private List<OrderTimelineResponse> timeline = new ArrayList<>();

    @Schema(description = "Ghi chú của khách hàng khi đặt đơn", example = "Giao giờ hành chính")
    private String note;

    @Schema(description = "Lý do hủy đơn nếu đơn ở trạng thái CANCELLED", example = "CHANGE_OF_MIND")
    private String cancelReason;

    @Schema(description = "Thời gian đặt đơn hàng")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian xác nhận đơn")
    private OffsetDateTime confirmedAt;

    @Schema(description = "Thời gian bắt đầu giao hàng")
    private OffsetDateTime shippedAt;

    @Schema(description = "Thời gian hoàn tất đơn")
    private OffsetDateTime completedAt;

    @Schema(description = "Thời gian hủy đơn")
    private OffsetDateTime cancelledAt;
}

