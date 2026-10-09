package com.chungpr0.bookhub.modules.inventory.dto.request;

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
@Schema(description = "Tham số lọc cảnh báo tồn kho")
public class LowStockFilterRequest {

    @Schema(description = "Trạng thái tồn kho: LOW_STOCK hoặc OUT_OF_STOCK (mặc định lấy cả hai)", example = "LOW_STOCK")
    private String stockStatus;

    @Schema(description = "Lọc theo ID danh mục", example = "5")
    private Long categoryId;
}

