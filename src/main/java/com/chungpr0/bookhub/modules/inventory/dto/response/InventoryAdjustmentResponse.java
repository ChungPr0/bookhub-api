package com.chungpr0.bookhub.modules.inventory.dto.response;

import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả điều chỉnh tồn kho")
public class InventoryAdjustmentResponse {

    @Schema(description = "ID bản ghi thẻ kho tương ứng", example = "701")
    private Long transactionId;

    @Schema(description = "Thông tin cuốn sách điều chỉnh")
    private SimpleBookResponse book;

    @Schema(description = "Loại điều chỉnh (ADJUST_IN hoặc ADJUST_OUT)", example = "ADJUST_OUT")
    private AdjustmentType type;

    @Schema(description = "Số lượng điều chỉnh", example = "5")
    private int quantity;

    @Schema(description = "Tồn kho trước khi điều chỉnh", example = "84")
    private int stockBefore;

    @Schema(description = "Tồn kho sau khi điều chỉnh", example = "79")
    private int stockAfter;

    @Schema(description = "Lý do điều chỉnh", example = "DAMAGED")
    private String reason;

    @Schema(description = "Danh sách các lô bị trừ tồn kho (đối với ADJUST_OUT)")
    @Builder.Default
    private List<AffectedBatchResponse> affectedBatches = new ArrayList<>();

    @Schema(description = "Thời điểm thực hiện điều chỉnh", example = "2026-10-09T17:35:00+07:00")
    private OffsetDateTime createdAt;
}

