package com.chungpr0.bookhub.modules.report.dto.response;

import com.chungpr0.bookhub.common.enums.CustomerTier;
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
public class CustomerReportResponse {

    @Schema(description = "Phân bổ khách hàng theo các phân hạng thành viên")
    private List<TierDistributionItem> tierDistribution;

    @Schema(description = "Chuỗi số lượng khách hàng mới đăng ký theo ngày")
    private List<NewCustomersSeriesItem> newCustomersSeries;

    @Schema(description = "Danh sách khách hàng có chi tiêu cao nhất")
    private List<TopCustomerItem> topCustomers;

    @Schema(description = "Tỷ lệ khách hàng mua lại ít nhất 2 đơn (%)", example = "38.5")
    private Double repeatPurchaseRate;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierDistributionItem {
        @Schema(description = "Hạng thành viên (BRONZE, SILVER, GOLD)", example = "BRONZE")
        private CustomerTier tier;

        @Schema(description = "Số lượng khách hàng", example = "1820")
        private long count;

        @Schema(description = "Tỷ lệ phần trăm (%)", example = "72.8")
        private Double percent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewCustomersSeriesItem {
        @Schema(description = "Ngày đăng ký (YYYY-MM-DD)", example = "2026-10-01")
        private String date;

        @Schema(description = "Số lượng khách đăng ký mới trong ngày", example = "18")
        private long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopCustomerItem {
        @Schema(description = "Thông tin cơ bản của khách hàng")
        private SimpleCustomerInfo customer;

        @Schema(description = "Số lượng đơn hàng đã đặt", example = "16")
        private long orderCount;

        @Schema(description = "Tổng số tiền đã chi tiêu (VNĐ)", example = "12450000")
        private Long totalSpent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleCustomerInfo {
        @Schema(description = "ID khách hàng", example = "45")
        private Long id;

        @Schema(description = "Họ và tên khách hàng", example = "Phạm Quốc Dũng")
        private String fullName;

        @Schema(description = "Số điện thoại", example = "0989111222")
        private String phone;

        @Schema(description = "Hạng thành viên", example = "GOLD")
        private CustomerTier tier;
    }
}

