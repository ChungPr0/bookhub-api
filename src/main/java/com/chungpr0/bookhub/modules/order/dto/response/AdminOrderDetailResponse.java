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
public class AdminOrderDetailResponse {

    @Schema(description = "ID đơn hàng", example = "8801")
    private Long id;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "Phiên bản dữ liệu (Optimistic Lock)", example = "1")
    private int version;

    @Schema(description = "Trạng thái đơn hàng", example = "CONFIRMED")
    private OrderStatus status;

    @Schema(description = "Trạng thái thanh toán", example = "PAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Phương thức thanh toán")
    private PaymentMethodResponse paymentMethod;

    @Schema(description = "Thông tin khách hàng")
    private OrderCustomerInfoResponse customer;

    @Schema(description = "Thông tin người nhận")
    private OrderReceiverResponse receiver;

    @Schema(description = "Ghi chú đơn hàng", example = "Giao giờ hành chính")
    private String note;

    @Schema(description = "Danh sách sản phẩm trong đơn kèm giá vốn và lợi nhuận")
    @Builder.Default
    private List<OrderItemDetailResponse> items = new ArrayList<>();

    @Schema(description = "Bảng kê khai chi phí và lợi nhuận gộp")
    private AdminOrderPricingResponse pricing;

    @Schema(description = "Lịch sử các giao dịch thanh toán")
    @Builder.Default
    private List<PaymentResponse> payments = new ArrayList<>();

    @Schema(description = "Danh sách hành động quản trị được phép làm (CONFIRM, SHIP, COMPLETE, CANCEL, CONFIRM_PAYMENT, RETURN, REFUND)", example = "[\"SHIP\", \"CANCEL\"]")
    @Builder.Default
    private List<String> allowedActions = new ArrayList<>();

    @Schema(description = "Thời gian tạo đơn")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian xác nhận đơn")
    private OffsetDateTime confirmedAt;

    @Schema(description = "Thời gian giao hàng")
    private OffsetDateTime shippedAt;

    @Schema(description = "Thời gian hoàn tất")
    private OffsetDateTime completedAt;

    @Schema(description = "Thời gian hủy đơn")
    private OffsetDateTime cancelledAt;

    @Schema(description = "Lý do hủy đơn")
    private String cancelReason;
}

