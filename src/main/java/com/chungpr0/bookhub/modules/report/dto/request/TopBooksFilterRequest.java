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
public class TopBooksFilterRequest {

    @Schema(description = "Ngày bắt đầu (YYYY-MM-DD)", example = "2026-09-01")
    private LocalDate from;

    @Schema(description = "Ngày kết thúc (YYYY-MM-DD)", example = "2026-10-07")
    private LocalDate to;

    @Schema(description = "Lọc theo danh mục sách", example = "1")
    private Long categoryId;

    @Min(value = 1, message = "Số lượng sách tối thiểu là 1")
    @Max(value = 100, message = "Số lượng sách tối đa là 100")
    @Schema(description = "Số lượng sách cần lấy (1-100, mặc định 20)", example = "20")
    @Builder.Default
    private Integer limit = 20;

    @Schema(description = "Tiêu chí xếp hạng: QUANTITY, REVENUE, PROFIT (Mặc định: QUANTITY)", example = "QUANTITY")
    @Builder.Default
    private String sortBy = "QUANTITY";
}

