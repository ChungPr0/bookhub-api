package com.chungpr0.bookhub.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết lô hàng (Batch)")
public class BatchResponse {

    @Schema(description = "ID lô hàng", example = "501")
    private Long id;

    @Schema(description = "Mã lô hàng", example = "BATCH-PN-20261009-A9X2-101")
    private String batchCode;

    @Schema(description = "Thông tin cuốn sách trong lô")
    private SimpleBookResponse book;

    @Schema(description = "Mã phiếu nhập liên kết", example = "PN-20261009-A9X2")
    private String receiptCode;

    @Schema(description = "Nhà cung cấp xuất hàng")
    private SimpleSupplierResponse supplier;

    @Schema(description = "Giá vốn nhập / 1 cuốn (VND)", example = "47400")
    private Long importPrice;

    @Schema(description = "Số lượng nhập ban đầu", example = "200")
    private int quantityImported;

    @Schema(description = "Số lượng tồn thực tế của lô (FIFO)", example = "184")
    private int quantityRemaining;

    @Schema(description = "Ngày nhập kho", example = "2026-10-09")
    private LocalDate importDate;

    @Schema(description = "Thời điểm lưu trữ lô", example = "2026-10-09T17:35:00+07:00")
    private OffsetDateTime createdAt;
}

