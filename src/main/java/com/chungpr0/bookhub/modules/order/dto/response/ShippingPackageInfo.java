package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.DeliveryRegion;
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
public class ShippingPackageInfo {

    @Schema(description = "Tổng số lượng cuốn sách trong kiện", example = "3")
    private int totalItems;

    @Schema(description = "Tổng cân nặng kiện hàng (gram)", example = "900")
    private int totalWeightGram;

    @Schema(description = "Phân vùng địa lý giao hàng (INNER_CITY hoặc OUTER_CITY)", example = "INNER_CITY")
    private DeliveryRegion region;

    @Schema(description = "Có đủ điều kiện được miễn phí vận chuyển hay không", example = "false")
    private boolean isFreeShippingEligible;

    @Schema(description = "Số tiền còn thiếu để đạt ngưỡng freeship (0 nếu đã đạt)", example = "51240")
    private Long amountToFreeShipping;
}

