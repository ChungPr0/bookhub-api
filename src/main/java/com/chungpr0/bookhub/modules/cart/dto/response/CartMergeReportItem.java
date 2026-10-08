package com.chungpr0.bookhub.modules.cart.dto.response;

import com.chungpr0.bookhub.common.enums.CartMergeResult;
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
@Schema(description = "Báo cáo kết quả gộp cho từng cuốn sách")
public class CartMergeReportItem {

    @Schema(description = "ID cuốn sách", example = "108")
    private Long bookId;

    @Schema(description = "Số lượng yêu cầu từ LocalStorage", example = "10")
    private Integer requestedQuantity;

    @Schema(description = "Số lượng thực tế được gộp vào giỏ", example = "3")
    private Integer mergedQuantity;

    @Schema(description = "Kết quả xử lý dòng sách", example = "ADJUSTED")
    private CartMergeResult result;

    @Schema(description = "Lý do chi tiết nếu bị điều chỉnh hoặc bỏ qua", example = "Số lượng đã được tự động giảm xuống 3 do tồn kho có hạn")
    private String reason;
}

