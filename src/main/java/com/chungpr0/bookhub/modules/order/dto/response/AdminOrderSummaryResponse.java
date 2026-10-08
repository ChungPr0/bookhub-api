package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderSummaryResponse {

    @Schema(description = "ID đơn hàng", example = "8801")
    private Long id;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "Thông tin khách hàng đặt đơn")
    private OrderCustomerInfoResponse customer;

    @Schema(description = "Tên người nhận hàng", example = "Nguyễn Tiến Chung")
    private String receiverName;

    @Schema(description = "Số điện thoại người nhận", example = "0988888888")
    private String receiverPhone;

    @Schema(description = "Trạng thái đơn hàng", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "Trạng thái thanh toán", example = "UNPAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Phương thức thanh toán", example = "VNPAY")
    private PaymentMethodCode paymentMethod;

    @Schema(description = "Hạn thanh toán đơn hàng online")
    private OffsetDateTime paymentExpiresAt;

    @Schema(description = "Số lượng đầu sách trong đơn", example = "2")
    private int itemCount;

    @Schema(description = "Tổng số lượng cuốn sách đặt mua", example = "4")
    private int totalQuantity;

    @Schema(description = "Tổng tiền thanh toán cuối cùng (VND)", example = "258760")
    private Long finalAmount;

    @Schema(description = "Thời gian đặt đơn hàng")
    private OffsetDateTime createdAt;
}

