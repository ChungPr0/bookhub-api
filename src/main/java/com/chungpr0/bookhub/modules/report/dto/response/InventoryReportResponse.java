package com.chungpr0.bookhub.modules.report.dto.response;

import com.chungpr0.bookhub.common.dto.PageResponse;
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
public class InventoryReportResponse {

    @Schema(description = "Số liệu tổng quan về tình trạng kho")
    private InventorySummary summary;

    @Schema(description = "Danh sách chi tiết các đầu sách phân trang")
    private PageResponse<InventoryReportItemResponse> items;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventorySummary {
        @Schema(description = "Tổng số đầu sách khác nhau", example = "1250")
        private long totalTitles;

        @Schema(description = "Tổng số lượng cuốn tồn trong kho", example = "45800")
        private long totalQuantity;

        @Schema(description = "Tổng giá trị vốn tồn kho theo FIFO (VNĐ)", example = "1832000000")
        private Long totalStockValue;

        @Schema(description = "Tổng giá trị bán lẻ dự kiến (VNĐ)", example = "3206000000")
        private Long totalRetailValue;

        @Schema(description = "Số đầu sách sắp hết hàng (tồn <= ngưỡng)", example = "18")
        private long lowStockCount;

        @Schema(description = "Số đầu sách đã hết sạch tồn kho (tồn = 0)", example = "5")
        private long outOfStockCount;

        @Schema(description = "Số đầu sách tồn đọng không có giao dịch bán trong 90 ngày (Hàng ế)", example = "42")
        private long deadStockCount;
    }
}

