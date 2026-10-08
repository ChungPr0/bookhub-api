package com.chungpr0.bookhub.modules.order.dto.request;

import com.chungpr0.bookhub.modules.order.enums.OrderCancelReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCancelOrderRequest {

    @Schema(description = "Lý do khách hàng hủy đơn", example = "CHANGE_OF_MIND")
    @NotNull(message = "Lý do hủy đơn không được để trống")
    private OrderCancelReason reason;

    @Schema(description = "Ghi chú chi tiết lý do hủy", example = "Mình muốn đổi sang mua cuốn sách khác")
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String reasonNote;
}

