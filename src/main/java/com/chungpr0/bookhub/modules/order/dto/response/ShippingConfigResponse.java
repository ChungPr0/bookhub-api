package com.chungpr0.bookhub.modules.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingConfigResponse {

    @Schema(description = "Cước giao hàng tiêu chuẩn (VND)", example = "30000")
    private Long standardBaseFee;

    @Schema(description = "Cước giao hàng nhanh (VND)", example = "45000")
    private Long expressBaseFee;

    @Schema(description = "Cước giao hàng hỏa tốc trong ngày (VND)", example = "60000")
    private Long sameDayBaseFee;

    @Schema(description = "Ngưỡng tiền sách để được miễn phí vận chuyển (VND)", example = "300000")
    private Long freeShippingThreshold;

    @Schema(description = "Mức hỗ trợ freeship tối đa (VND)", example = "30000")
    private Long maxFreeShippingSubsidy;

    @Schema(description = "Định mức cân nặng cơ bản của kiện (gram)", example = "2000")
    private int standardMaxWeightGram;

    @Schema(description = "Đơn vị tính vượt cân (gram)", example = "500")
    private int overweightUnitGram;

    @Schema(description = "Phụ phí vượt cân trên mỗi đơn vị (VND)", example = "5000")
    private Long overweightSurcharge;

    @Schema(description = "Thời điểm cập nhật cấu hình gần nhất")
    private OffsetDateTime updatedAt;

    @Schema(description = "Thông tin người thực hiện cập nhật cấu hình")
    private StaffOrAccountInfoResponse updatedBy;
}

