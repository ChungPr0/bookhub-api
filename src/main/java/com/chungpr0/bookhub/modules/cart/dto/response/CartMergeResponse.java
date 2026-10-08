package com.chungpr0.bookhub.modules.cart.dto.response;

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
@Schema(description = "Kết quả sau khi gộp giỏ hàng vãng lai")
public class CartMergeResponse {

    @Schema(description = "Dữ liệu giỏ hàng mới nhất sau khi gộp")
    private CartResponse cart;

    @Schema(description = "Báo cáo chi tiết quá trình gộp từng sản phẩm")
    @Builder.Default
    private List<CartMergeReportItem> mergeReport = new ArrayList<>();
}

