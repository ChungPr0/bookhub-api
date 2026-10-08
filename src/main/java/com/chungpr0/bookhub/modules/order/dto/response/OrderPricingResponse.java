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
public class OrderPricingResponse {

    @Schema(description = "Tổng tiền hàng tạm tính trước giảm giá (VND)", example = "276400")
    private Long subtotal;

    @Schema(description = "Mã voucher đã áp dụng thành công (null nếu không có)", example = "BOOKHUB10")
    private String voucherCode;

    @Schema(description = "Số tiền giảm từ voucher (VND)", example = "27640")
    @Builder.Default
    private Long voucherDiscount = 0L;

    @Schema(description = "Số điểm thưởng đã dùng", example = "200")
    @Builder.Default
    private Integer pointsUsed = 0;

    @Schema(description = "Số tiền giảm quy đổi từ điểm thưởng (VND)", example = "20000")
    @Builder.Default
    private Long pointsDiscount = 0L;

    @Schema(description = "Phí vận chuyển thực tế (VND)", example = "30000")
    @Builder.Default
    private Long shippingFee = 0L;

    @Schema(description = "Ngưỡng giá trị đơn để được miễn phí vận chuyển (VND)", example = "300000")
    private Long freeShippingThreshold;

    @Schema(description = "Số tiền cần mua thêm để được miễn phí vận chuyển (0 nếu đã được freeship)", example = "51240")
    private Long amountToFreeShipping;

    @Schema(description = "Số tiền thanh toán cuối cùng (VND)", example = "258760")
    private Long finalAmount;
}

