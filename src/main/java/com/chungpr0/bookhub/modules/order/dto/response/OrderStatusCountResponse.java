package com.chungpr0.bookhub.modules.order.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class OrderStatusCountResponse {

    @JsonProperty("ALL")
    @Schema(description = "Tổng tất cả đơn hàng", example = "150")
    private long all;

    @JsonProperty("PENDING")
    @Schema(description = "Số đơn chờ xác nhận", example = "12")
    private long pending;

    @JsonProperty("CONFIRMED")
    @Schema(description = "Số đơn đã xác nhận", example = "25")
    private long confirmed;

    @JsonProperty("SHIPPING")
    @Schema(description = "Số đơn đang giao", example = "40")
    private long shipping;

    @JsonProperty("COMPLETED")
    @Schema(description = "Số đơn đã hoàn tất", example = "65")
    private long completed;

    @JsonProperty("CANCELLED")
    @Schema(description = "Số đơn đã hủy", example = "7")
    private long cancelled;

    @JsonProperty("RETURNED")
    @Schema(description = "Số đơn đã hoàn trả", example = "1")
    private long returned;

    @Schema(description = "Cảnh báo các đơn cần nhân viên chú ý xử lý")
    private NeedsAttentionResponse needsAttention;
}

