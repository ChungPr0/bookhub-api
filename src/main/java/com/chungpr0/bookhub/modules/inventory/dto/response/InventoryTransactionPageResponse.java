package com.chungpr0.bookhub.modules.inventory.dto.response;

import com.chungpr0.bookhub.common.dto.PageMeta;
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
@Schema(description = "Dữ liệu phản hồi trang Thẻ kho")
public class InventoryTransactionPageResponse {

    @Schema(description = "Bảng tổng hợp thẻ kho (chỉ hiển thị khi có lọc theo bookId)")
    private InventoryCardSummaryResponse summary;

    @Schema(description = "Danh sách các dòng thẻ kho")
    @Builder.Default
    private List<InventoryTransactionResponse> items = new ArrayList<>();

    @Schema(description = "Thông tin phân trang")
    private PageMeta page;
}

