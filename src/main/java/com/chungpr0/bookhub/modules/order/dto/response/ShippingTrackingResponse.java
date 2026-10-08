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
public class ShippingTrackingResponse {

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "Trạng thái vận chuyển hiện tại", example = "SHIPPING")
    private String currentStatus;

    @Schema(description = "Đơn vị vận chuyển hợp tác", example = "Giao Hàng Nhanh (GHN)")
    private String carrier;

    @Schema(description = "Mã vận đơn đối tác", example = "GHN88921")
    private String trackingNumber;

    @Schema(description = "Danh sách mốc hành trình bưu kiện")
    @Builder.Default
    private List<ShippingMilestoneResponse> events = new ArrayList<>();
}

