package com.chungpr0.bookhub.modules.inventory.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Kết quả sau khi lập phiếu nhập kho")
public class CreateStockReceiptResponse {

    @Schema(description = "Thông tin chi tiết phiếu nhập vừa tạo")
    private StockReceiptDetailResponse receipt;

    @Schema(description = "Danh sách các cảnh báo nghiệp vụ (nếu có)")
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}

