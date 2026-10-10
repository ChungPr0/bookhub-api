package com.chungpr0.bookhub.modules.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    @Schema(description = "Thông tin chu kỳ báo cáo")
    private PeriodInfo period;

    @Schema(description = "Chỉ số doanh thu thuần")
    private MetricChange revenue;

    @Schema(description = "Chỉ số đơn hàng")
    private OrdersMetric orders;

    @Schema(description = "Chỉ số khách hàng mới")
    private MetricChange newCustomers;

    @Schema(description = "Giá trị đơn hàng trung bình (VNĐ)", example = "321875")
    private Long avgOrderValue;

    @Schema(description = "Tổng số lượng cuốn sách đã bán", example = "96")
    private long itemsSold;

    @Schema(description = "Danh sách cảnh báo công việc cần xử lý gấp")
    private PendingActions pendingActions;

    @Schema(description = "Chuỗi dữ liệu biểu đồ doanh thu theo các mốc thời gian")
    private List<ChartPoint> revenueChart;

    @Schema(description = "Top sách bán chạy nhất trong chu kỳ")
    private List<TopBookItem> topBooks;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodInfo {
        @Schema(description = "Loại chu kỳ (TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH)", example = "TODAY")
        private String type;

        @Schema(description = "Thời điểm bắt đầu", example = "2026-10-07T00:00:00+07:00")
        private OffsetDateTime from;

        @Schema(description = "Thời điểm kết thúc", example = "2026-10-07T23:59:59+07:00")
        private OffsetDateTime to;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricChange {
        @Schema(description = "Giá trị hiện tại", example = "15450000")
        private Long value;

        @Schema(description = "Giá trị chu kỳ trước liền kề", example = "12800000")
        private Long previousValue;

        @Schema(description = "Tỷ lệ tăng trưởng %", example = "20.7")
        private Double changePercent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdersMetric {
        @Schema(description = "Tổng số đơn hàng", example = "48")
        private Long total;

        @Schema(description = "Tổng đơn chu kỳ trước", example = "40")
        private Long previousTotal;

        @Schema(description = "Tỷ lệ thay đổi %", example = "20.0")
        private Double changePercent;

        @Schema(description = "Phân bổ đơn theo trạng thái")
        private Map<String, Long> byStatus;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingActions {
        @Schema(description = "Đơn hàng mới chờ xác nhận", example = "6")
        private long pendingOrders;

        @Schema(description = "Đơn hàng chuyển khoản chưa thanh toán", example = "2")
        private long unpaidBankTransfers;

        @Schema(description = "Yêu cầu hoàn tiền cần xử lý", example = "1")
        private long refundPending;

        @Schema(description = "Đầu sách sắp hết hàng trong kho", example = "5")
        private long lowStockBooks;

        @Schema(description = "Đầu sách đã hết sạch tồn kho", example = "2")
        private long outOfStockBooks;

        @Schema(description = "Bài đánh giá của độc giả chưa được phản hồi", example = "8")
        private long reviewsWithoutReply;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPoint {
        @Schema(description = "Nhãn thời gian hiển thị", example = "08:00")
        private String timeLabel;

        @Schema(description = "Doanh thu (VNĐ)", example = "1250000")
        private Long revenue;

        @Schema(description = "Số lượng đơn hàng", example = "4")
        private long orderCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopBookItem {
        @Schema(description = "Thông tin cơ bản của cuốn sách")
        private SimpleBookInfo book;

        @Schema(description = "Số lượng cuốn đã bán", example = "24")
        private int quantitySold;

        @Schema(description = "Doanh thu từ cuốn sách này (VNĐ)", example = "1896000")
        private Long revenue;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleBookInfo {
        @Schema(description = "ID sách", example = "1")
        private Long id;

        @Schema(description = "Tên sách", example = "Nhà Giả Kim")
        private String title;

        @Schema(description = "Đường dẫn slug", example = "nha-gia-kim")
        private String slug;

        @Schema(description = "Ảnh bìa sách", example = "https://cdn.bookhub.vn/books/nha-gia-kim-cover.webp")
        private String thumbnailUrl;
    }
}

