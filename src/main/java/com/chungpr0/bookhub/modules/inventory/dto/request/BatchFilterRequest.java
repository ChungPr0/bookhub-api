package com.chungpr0.bookhub.modules.inventory.dto.request;

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
@Schema(description = "Tham số lọc danh sách lô hàng")
public class BatchFilterRequest {

    @Schema(description = "Lọc các lô hàng của riêng một cuốn sách", example = "101")
    private Long bookId;

    @Schema(description = "Lọc theo ID phiếu nhập kho", example = "88")
    private Long receiptId;

    @Schema(description = "Lọc theo ID nhà cung cấp", example = "1")
    private Long supplierId;

    @Schema(description = "true = chỉ lấy các lô còn hàng tồn (> 0)", example = "true")
    private Boolean hasRemaining;
}

