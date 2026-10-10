package com.chungpr0.bookhub.modules.report.dto.request;

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
public class InventoryReportFilterRequest {

    @Schema(description = "Lọc theo ID danh mục", example = "1")
    private Long categoryId;

    @Schema(description = "Lọc theo ID nhà xuất bản", example = "2")
    private Long publisherId;

    @Schema(description = "Lọc theo trạng thái tồn kho (IN_STOCK, LOW_STOCK, OUT_OF_STOCK)", example = "LOW_STOCK")
    private String stockStatus;
}

