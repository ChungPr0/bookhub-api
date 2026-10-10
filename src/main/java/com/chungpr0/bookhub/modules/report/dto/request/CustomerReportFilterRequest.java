package com.chungpr0.bookhub.modules.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CustomerReportFilterRequest {

    @Schema(description = "Ngày bắt đầu (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate from;

    @Schema(description = "Ngày kết thúc (YYYY-MM-DD)", example = "2026-10-07")
    private LocalDate to;

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    @Max(value = 50, message = "Số lượng tối đa là 50")
    @Schema(description = "Số lượng khách hàng mua nhiều nhất (Mặc định: 10)", example = "10")
    @Builder.Default
    private Integer limit = 10;
}

