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
@Schema(description = "Thông tin tóm tắt phiếu nhập kho")
public class StockReceiptSummaryResponse {

    @Schema(description = "ID phiếu nhập", example = "88")
    private Long id;

    @Schema(description = "Mã phiếu nhập kho", example = "PN-20261009-A9X2")
    private String receiptCode;

    @Schema(description = "Nhà cung cấp xuất hàng")
    private SimpleSupplierResponse supplier;

    @Schema(description = "Ghi chú đợt nhập", example = "Nhập sách đợt 1")
    private String note;

    @Schema(description = "Ngày nhập kho", example = "2026-10-09")
    private LocalDate importDate;

    @Schema(description = "Tổng số lượng sách nhập", example = "300")
    private int totalQuantity;

    @Schema(description = "Tổng chi phí nhập kho (VND)", example = "16980000")
    private Long totalCost;

    @Schema(description = "Tài khoản nhân viên lập phiếu")
    private SimpleUserResponse createdBy;

    @Schema(description = "Thời điểm tạo phiếu trên hệ thống", example = "2026-10-09T17:35:00+07:00")
    private OffsetDateTime createdAt;
}

