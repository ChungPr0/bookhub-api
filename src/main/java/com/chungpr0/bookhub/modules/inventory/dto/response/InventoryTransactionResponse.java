package com.chungpr0.bookhub.modules.inventory.dto.response;

import com.chungpr0.bookhub.modules.inventory.enums.InventoryTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin dòng giao dịch thẻ kho")
public class InventoryTransactionResponse {

    @Schema(description = "ID bản ghi thẻ kho", example = "992")
    private Long id;

    @Schema(description = "Loại biến động (IMPORT, SALE, ORDER_CANCEL, ORDER_RETURN, ADJUST_IN, ADJUST_OUT)", example = "ADJUST_OUT")
    private InventoryTransactionType type;

    @Schema(description = "Số lượng biến động (dương = tăng, âm = giảm)", example = "-5")
    private int quantity;

    @Schema(description = "Tồn kho sau biến động", example = "79")
    private int stockAfter;

    @Schema(description = "Chứng từ tham chiếu")
    private SimpleReferenceResponse reference;

    @Schema(description = "Lý do (nếu là điều chỉnh)", example = "DAMAGED")
    private String reason;

    @Schema(description = "Ghi chú giải trình", example = "Sách bị dính nước mưa")
    private String note;

    @Schema(description = "Người thực hiện thao tác")
    private SimpleUserResponse createdBy;

    @Schema(description = "Thời điểm ghi nhận biến động", example = "2026-10-09T17:35:00+07:00")
    private OffsetDateTime createdAt;
}

