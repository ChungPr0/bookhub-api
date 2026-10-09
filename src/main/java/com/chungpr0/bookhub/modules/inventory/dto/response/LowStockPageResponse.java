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
@Schema(description = "Dữ liệu phản hồi trang Cảnh báo tồn kho")
public class LowStockPageResponse {

    @Schema(description = "Tổng kết số lượng cảnh báo")
    private LowStockCountSummaryResponse summary;

    @Schema(description = "Danh sách các đầu sách cảnh báo tồn kho")
    @Builder.Default
    private List<LowStockBookResponse> items = new ArrayList<>();

    @Schema(description = "Thông tin phân trang")
    private PageMeta page;
}

