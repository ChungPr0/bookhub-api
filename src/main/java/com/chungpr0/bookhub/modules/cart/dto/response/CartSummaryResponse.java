package com.chungpr0.bookhub.modules.cart.dto.response;

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
@Schema(description = "Tổng kết thông tin tài chính và số lượng giỏ hàng")
public class CartSummaryResponse {

    @Schema(description = "Số lượng đầu sách khác nhau trong giỏ", example = "2")
    private Integer itemCount;

    @Schema(description = "Tổng số lượng cuốn sách trong giỏ", example = "7")
    private Integer totalQuantity;

    @Schema(description = "Tổng tiền toàn bộ các món trong giỏ (VND)", example = "558400")
    private Long subtotal;

    @Schema(description = "Tổng tiền các món có trạng thái khả dụng AVAILABLE có thể thanh toán ngay (VND)", example = "126400")
    private Long selectableSubtotal;

    @Schema(description = "Có sản phẩm nào không khả dụng hoặc thiếu hàng hay không", example = "true")
    private Boolean hasUnavailableItems;
}

