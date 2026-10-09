package com.chungpr0.bookhub.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin cuốn sách trong danh sách cảnh báo tồn kho")
public class LowStockBookResponse {

    @Schema(description = "Thông tin cuốn sách")
    private SimpleBookResponse book;

    @Schema(description = "Số lượng tồn kho hiện tại", example = "4")
    private int stockQuantity;

    @Schema(description = "Ngưỡng cảnh báo tồn kho tối thiểu", example = "10")
    private int lowStockThreshold;

    @Schema(description = "Trạng thái tồn kho (LOW_STOCK hoặc OUT_OF_STOCK)", example = "LOW_STOCK")
    private String stockStatus;

    @Schema(description = "Số lượng đã bán trong 30 ngày qua", example = "60")
    private int soldLast30Days;

    @Schema(description = "Ước tính số ngày còn lại sẽ hết hàng", example = "2")
    private int estimatedDaysOfStock;

    @Schema(description = "Ngày nhập kho gần nhất (YYYY-MM-DD)", example = "2026-08-15")
    private LocalDate lastImportDate;

    @Schema(description = "Nhà cung cấp của lần nhập gần nhất")
    private SimpleSupplierResponse lastSupplier;
}

