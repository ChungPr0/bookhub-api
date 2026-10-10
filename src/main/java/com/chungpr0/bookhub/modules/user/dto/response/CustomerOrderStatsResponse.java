package com.chungpr0.bookhub.modules.user.dto.response;

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
public class CustomerOrderStatsResponse {

    @Schema(description = "Tổng số lượng đơn hàng đã đặt", example = "12")
    private long totalOrders;

    @Schema(description = "Số đơn hàng đã hoàn tất thành công", example = "9")
    private long completedOrders;

    @Schema(description = "Số đơn hàng đã bị hủy", example = "2")
    private long cancelledOrders;

    @Schema(description = "Số đơn hàng đã hoàn trả", example = "1")
    private long returnedOrders;

    @Schema(description = "Giá trị đơn hàng trung bình (VNĐ)", example = "325000")
    private Long avgOrderValue;
}

