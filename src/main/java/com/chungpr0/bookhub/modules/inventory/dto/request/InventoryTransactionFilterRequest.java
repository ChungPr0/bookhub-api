package com.chungpr0.bookhub.modules.inventory.dto.request;

import com.chungpr0.bookhub.modules.inventory.enums.InventoryTransactionType;
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
@Schema(description = "Tham số lọc lịch sử thẻ kho")
public class InventoryTransactionFilterRequest {

    @Schema(description = "Lọc thẻ kho của một cuốn sách", example = "101")
    private Long bookId;

    @Schema(description = "Loại biến động (IMPORT, SALE, ORDER_CANCEL, ORDER_RETURN, ADJUST_IN, ADJUST_OUT)", example = "IMPORT")
    private InventoryTransactionType type;

    @Schema(description = "Ngày phát sinh từ (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate createdFrom;

    @Schema(description = "Ngày phát sinh đến (YYYY-MM-DD)", example = "2026-10-31")
    private LocalDate createdTo;
}

