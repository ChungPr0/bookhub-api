package com.chungpr0.bookhub.modules.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {

    @Schema(description = "Số liệu tổng hợp tài chính trong kỳ báo cáo")
    private RevenueSummary summary;

    @Schema(description = "Chuỗi số liệu doanh thu và lợi nhuận theo từng mốc chu kỳ (ngày/tuần/tháng)")
    private List<RevenueSeriesPoint> series;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSummary {
        @Schema(description = "Tổng doanh số bán hàng gộp (VNĐ)", example = "165000000")
        private Long grossSales;

        @Schema(description = "Tổng giá trị giảm giá từ mã voucher (VNĐ)", example = "5000000")
        private Long voucherDiscount;

        @Schema(description = "Tổng giá trị giảm giá từ điểm tích lũy (VNĐ)", example = "2000000")
        private Long pointsDiscount;

        @Schema(description = "Doanh thu thuần (VNĐ)", example = "158000000")
        private Long netRevenue;

        @Schema(description = "Giá vốn hàng bán theo FIFO (VNĐ)", example = "94800000")
        private Long costOfGoods;

        @Schema(description = "Lợi nhuận gộp (VNĐ)", example = "63200000")
        private Long grossProfit;

        @Schema(description = "Tỷ suất lợi nhuận gộp (%)", example = "40.0")
        private Double grossMarginPercent;

        @Schema(description = "Tổng số đơn hàng đã hoàn tất", example = "450")
        private long completedOrders;

        @Schema(description = "Số đơn hàng đã hoàn trả", example = "3")
        private long returnedOrders;

        @Schema(description = "Số tiền hoàn trả cho khách (VNĐ)", example = "950000")
        private Long returnedAmount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueSeriesPoint {
        @Schema(description = "Mốc thời gian bắt đầu chu kỳ (YYYY-MM-DD)", example = "2026-10-01")
        private String periodStart;

        @Schema(description = "Nhãn hiển thị trên biểu đồ", example = "01/10/2026")
        private String periodLabel;

        @Schema(description = "Doanh thu thuần trong mốc này (VNĐ)", example = "24500000")
        private Long netRevenue;

        @Schema(description = "Giá vốn trong mốc này (VNĐ)", example = "14700000")
        private Long costOfGoods;

        @Schema(description = "Lợi nhuận gộp trong mốc này (VNĐ)", example = "9800000")
        private Long grossProfit;

        @Schema(description = "Số đơn hàng hoàn tất trong mốc", example = "72")
        private long orderCount;

        @Schema(description = "Số lượng cuốn sách đã bán", example = "150")
        private long itemsSold;
    }
}

