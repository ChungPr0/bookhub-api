package com.chungpr0.bookhub.modules.inventory.dto.response;

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
@Schema(description = "Bảng tổng hợp thẻ kho của một cuốn sách")
public class InventoryCardSummaryResponse {

    @Schema(description = "ID cuốn sách", example = "101")
    private Long bookId;

    @Schema(description = "Tên cuốn sách", example = "Nhà Giả Kim")
    private String bookTitle;

    @Schema(description = "Tồn kho đầu kỳ trong khoảng thời gian lọc", example = "50")
    private int openingStock;

    @Schema(description = "Tổng số lượng nhập trong kỳ", example = "200")
    private int totalImported;

    @Schema(description = "Tổng số lượng xuất bán trong kỳ", example = "166")
    private int totalSold;

    @Schema(description = "Tổng số lượng điều chỉnh (tăng/giảm) trong kỳ", example = "-5")
    private int totalAdjusted;

    @Schema(description = "Tồn kho cuối kỳ trong khoảng thời gian lọc", example = "79")
    private int closingStock;
}

