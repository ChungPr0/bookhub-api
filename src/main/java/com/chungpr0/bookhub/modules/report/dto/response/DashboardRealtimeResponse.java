package com.chungpr0.bookhub.modules.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRealtimeResponse {

    @Schema(description = "Thời điểm tổng hợp số liệu", example = "2026-10-07T19:35:00+07:00")
    private OffsetDateTime asOfTime;

    @Schema(description = "Tổng quan vận hành trong ngày")
    private TodaySummary todaySummary;

    @Schema(description = "Phân tích doanh thu và số đơn theo từng khung giờ trong ngày")
    private List<HourlyBreakdown> hourlyBreakdown;

    @Schema(description = "Hàng đợi công việc khẩn cấp")
    private UrgentQueues urgentQueues;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaySummary {
        @Schema(description = "Doanh thu gộp trong ngày (VNĐ)", example = "16890000")
        private Long todayGrossRevenue;

        @Schema(description = "Doanh thu thuần trong ngày (VNĐ)", example = "15450000")
        private Long todayNetRevenue;

        @Schema(description = "Tổng số đơn hàng phát sinh trong ngày", example = "48")
        private long todayOrdersCount;

        @Schema(description = "Số đơn hàng đã hoàn tất trong ngày", example = "9")
        private long todayCompletedCount;

        @Schema(description = "Số đơn hàng bị hủy trong ngày", example = "1")
        private long todayCancelledCount;

        @Schema(description = "Tỷ lệ hủy đơn trong ngày (%)", example = "2.08")
        private Double cancellationRate;

        @Schema(description = "Thời gian xử lý đơn trung bình (phút)", example = "35")
        private Integer averageProcessingTimeMinutes;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyBreakdown {
        @Schema(description = "Khung giờ trong ngày (0-23)", example = "8")
        private int hour;

        @Schema(description = "Số lượng đơn hàng", example = "4")
        private long orders;

        @Schema(description = "Doanh thu phát sinh (VNĐ)", example = "1250000")
        private Long revenue;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UrgentQueues {
        @Schema(description = "Số đơn hàng mới chờ xác nhận", example = "6")
        private long unconfirmedOrdersCount;

        @Schema(description = "Số đơn hàng đang chờ giao vận chuyển", example = "14")
        private long pendingShipmentCount;

        @Schema(description = "Số đơn chuyển khoản chưa thanh toán", example = "2")
        private long unpaidBankTransferCount;

        @Schema(description = "Số yêu cầu đổi trả hàng cần giải quyết", example = "1")
        private long returnRequestsCount;
    }
}

