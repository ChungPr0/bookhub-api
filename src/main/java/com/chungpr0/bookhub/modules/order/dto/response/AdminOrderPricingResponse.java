package com.chungpr0.bookhub.modules.order.dto.response;

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
public class AdminOrderPricingResponse {

    @Schema(description = "Tổng tiền hàng ban đầu (VND)", example = "126400")
    private Long subtotal;

    @Schema(description = "Mã voucher áp dụng", example = "BOOKHUB10")
    private String voucherCode;

    @Schema(description = "Tiền giảm từ voucher (VND)", example = "0")
    @Builder.Default
    private Long voucherDiscount = 0L;

    @Schema(description = "Số điểm thưởng đã dùng", example = "0")
    @Builder.Default
    private Integer pointsUsed = 0;

    @Schema(description = "Tiền giảm từ điểm thưởng (VND)", example = "0")
    @Builder.Default
    private Long pointsDiscount = 0L;

    @Schema(description = "Phí giao hàng (VND)", example = "30000")
    @Builder.Default
    private Long shippingFee = 0L;

    @Schema(description = "Tổng tiền khách phải trả (VND)", example = "156400")
    private Long finalAmount;

    @Schema(description = "Tổng giá vốn hàng bán FIFO (VND)", example = "94800")
    @Builder.Default
    private Long totalCost = 0L;

    @Schema(description = "Lợi nhuận gộp ước tính (finalAmount - shippingFee - totalCost) (VND)", example = "31600")
    @Builder.Default
    private Long estimatedGrossProfit = 0L;
}

