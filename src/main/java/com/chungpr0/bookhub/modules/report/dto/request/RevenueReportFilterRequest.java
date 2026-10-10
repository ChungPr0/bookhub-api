package com.chungpr0.bookhub.modules.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportFilterRequest {

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Schema(description = "Ngày bắt đầu (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate from;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Schema(description = "Ngày kết thúc (YYYY-MM-DD)", example = "2026-10-07")
    private LocalDate to;

    @Schema(description = "Chu kỳ nhóm số liệu (DAY, WEEK, MONTH). Mặc định là DAY", example = "DAY")
    @Builder.Default
    private String groupBy = "DAY";

    @Schema(description = "Lọc theo danh mục sách", example = "1")
    private Long categoryId;

    @Schema(description = "Lọc theo mã phương thức thanh toán (COD, BANK_TRANSFER, VNPAY)", example = "VNPAY")
    private String paymentMethod;
}

