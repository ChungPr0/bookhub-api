package com.chungpr0.bookhub.modules.order.dto.response;

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
public class ShippingCalculateResponse {

    @Schema(description = "Thông tin tổng hợp kiện hàng và phân vùng")
    private ShippingPackageInfo packageInfo;

    @Schema(description = "Bảng cước phí tính toán cho từng gói giao hàng khả dụng")
    @Builder.Default
    private List<CalculatedShippingOptionResponse> services = new ArrayList<>();
}

