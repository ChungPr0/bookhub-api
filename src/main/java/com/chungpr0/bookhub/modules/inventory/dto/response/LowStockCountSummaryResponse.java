package com.chungpr0.bookhub.modules.inventory.dto.response;

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
@Schema(description = "Tổng kết số lượng cảnh báo tồn kho")
public class LowStockCountSummaryResponse {

    @Schema(description = "Số lượng đầu sách sắp hết hàng (tồn kho <= ngưỡng)", example = "8")
    private long lowStockCount;

    @Schema(description = "Số lượng đầu sách đã hết hàng hoàn toàn (tồn kho = 0)", example = "3")
    private long outOfStockCount;
}

