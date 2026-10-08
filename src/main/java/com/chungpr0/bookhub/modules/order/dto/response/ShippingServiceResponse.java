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
public class ShippingServiceResponse {

    @Schema(description = "Mã gói giao hàng (STANDARD, EXPRESS, SAME_DAY)", example = "STANDARD")
    private ShippingServiceCode code;

    @Schema(description = "Tên gói giao hàng", example = "Giao hàng tiêu chuẩn")
    private String name;

    @Schema(description = "Mô tả gói giao hàng", example = "Chuyển phát đường bộ an toàn, tiết kiệm chi phí tối đa")
    private String description;

    @Schema(description = "Cước phí cơ bản (VND)", example = "30000")
    private Long baseFee;

    @Schema(description = "Thời gian giao tối thiểu (ngày)", example = "2")
    private int estimatedDaysMin;

    @Schema(description = "Thời gian giao tối đa (ngày)", example = "4")
    private int estimatedDaysMax;

    @Schema(description = "Khu vực địa lý hỗ trợ (ALL hoặc INNER_CITY_ONLY)", example = "ALL")
    private String supportedRegions;
}

