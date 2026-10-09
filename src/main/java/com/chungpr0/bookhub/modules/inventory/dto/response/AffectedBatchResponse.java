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
@Schema(description = "Thông tin lô hàng bị ảnh hưởng khi xuất kho điều chỉnh")
public class AffectedBatchResponse {

    @Schema(description = "Mã lô hàng bị trừ tồn kho", example = "BATCH-PN-20261009-A9X2-101")
    private String batchCode;

    @Schema(description = "Số lượng đã trừ khỏi lô này", example = "5")
    private int quantityDeducted;

    @Schema(description = "Số lượng còn lại trong lô sau khi trừ", example = "179")
    private int remainingInBatch;
}

