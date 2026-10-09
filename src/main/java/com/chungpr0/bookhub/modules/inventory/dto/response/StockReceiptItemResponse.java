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
@Schema(description = "Chi tiết một dòng sách / lô hàng trong phiếu nhập kho")
public class StockReceiptItemResponse {

    @Schema(description = "ID lô hàng vừa tạo", example = "501")
    private Long batchId;

    @Schema(description = "Mã lô nhập kho", example = "BATCH-PN-20261009-A9X2-101")
    private String batchCode;

    @Schema(description = "Thông tin cuốn sách")
    private SimpleBookResponse book;

    @Schema(description = "Đơn giá nhập / 1 cuốn (VND)", example = "47400")
    private Long importPrice;

    @Schema(description = "Số lượng nhập ban đầu", example = "200")
    private int quantityImported;

    @Schema(description = "Số lượng tồn hiện tại của lô", example = "200")
    private int quantityRemaining;

    @Schema(description = "Thành tiền của dòng sách (VND)", example = "9480000")
    private Long lineTotal;
}

