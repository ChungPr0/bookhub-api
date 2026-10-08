package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.ShippingServiceCode;
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
public class CalculatedShippingOptionResponse {

    @Schema(description = "Mã gói giao hàng", example = "STANDARD")
    private ShippingServiceCode code;

    @Schema(description = "Tên gói giao hàng", example = "Giao hàng tiêu chuẩn")
    private String name;

    @Schema(description = "Cước phí cơ bản (VND)", example = "30000")
    private Long baseFee;

    @Schema(description = "Phụ phí vượt cân nặng (VND)", example = "0")
    private Long overweightFee;

    @Schema(description = "Số tiền được miễn giảm phí vận chuyển (VND)", example = "0")
    private Long discountFee;

    @Schema(description = "Cước phí thực tế cuối cùng khách phải trả (VND)", example = "30000")
    private Long finalFee;

    @Schema(description = "Số ngày giao tối thiểu", example = "2")
    private int estimatedDaysMin;

    @Schema(description = "Số ngày giao tối đa", example = "4")
    private int estimatedDaysMax;
}

