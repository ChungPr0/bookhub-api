package com.chungpr0.bookhub.modules.inventory.dto.request;

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
@Schema(description = "Tham số tìm kiếm và lọc phiếu nhập kho")
public class StockReceiptFilterRequest {

    @Schema(description = "Từ khóa tìm kiếm theo mã phiếu hoặc ghi chú", example = "PN-20261009")
    private String keyword;

    @Schema(description = "Lọc theo ID nhà cung cấp", example = "1")
    private Long supplierId;

    @Schema(description = "Lọc theo ID tài khoản tạo phiếu", example = "1")
    private Long createdBy;

    @Schema(description = "Ngày nhập từ (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate importDateFrom;

    @Schema(description = "Ngày nhập đến (YYYY-MM-DD)", example = "2026-10-31")
    private LocalDate importDateTo;
}

