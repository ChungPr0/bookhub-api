package com.chungpr0.bookhub.modules.user.dto.response;

import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
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
public class CustomerOrderHistoryResponse {

    @Schema(description = "ID đơn hàng", example = "105")
    private Long id;

    @Schema(description = "Mã đơn hàng", example = "ORD-20261009-ABC123")
    private String orderCode;

    @Schema(description = "Thời gian đặt hàng", example = "2026-10-09T14:30:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Tổng tiền thanh toán cuối cùng (VNĐ)", example = "450000")
    private Long finalAmount;

    @Schema(description = "Trạng thái đơn hàng", example = "COMPLETED")
    private OrderStatus status;

    @Schema(description = "Trạng thái thanh toán", example = "PAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Số lượng mặt hàng trong đơn", example = "3")
    private int itemCount;

    public static CustomerOrderHistoryResponse fromEntity(Order order) {
        int count = order.getOrderDetails() != null ? order.getOrderDetails().size() : 0;
        return CustomerOrderHistoryResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .createdAt(order.getCreatedAt())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .itemCount(count)
                .build();
    }
}

