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
public class AdminCancelOrderRequest {

    @Schema(description = "Lý do nhà sách hủy đơn hàng", example = "DELIVERY_FAILED")
    @NotNull(message = "Lý do hủy đơn không được để trống")
    private OrderCancelReason reason;

    @Schema(description = "Ghi chú chi tiết nguyên nhân hủy đơn", example = "Shipper liên hệ giao hàng 3 lần nhưng khách không nghe máy")
    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;

    @Schema(description = "Phiên bản dữ liệu (Optimistic Lock)", example = "2")
    @NotNull(message = "Phiên bản dữ liệu (version) không được để trống")
    private Integer version;
}

