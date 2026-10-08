package com.chungpr0.bookhub.modules.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class UpdateShippingConfigRequest {

    @Schema(description = "Cước vận chuyển tiêu chuẩn mặc định (VND)", example = "30000")
    @NotNull(message = "Cước tiêu chuẩn không được để trống")
    @Min(value = 0, message = "Cước tiêu chuẩn không được âm")
    private Long standardBaseFee;

    @Schema(description = "Cước vận chuyển nhanh (VND)", example = "45000")
    @NotNull(message = "Cước giao nhanh không được để trống")
    @Min(value = 0, message = "Cước giao nhanh không được âm")
    private Long expressBaseFee;

    @Schema(description = "Cước giao hàng hỏa tốc trong ngày (VND)", example = "60000")
    @NotNull(message = "Cước hỏa tốc không được để trống")
    @Min(value = 0, message = "Cước hỏa tốc không được âm")
    private Long sameDayBaseFee;

    @Schema(description = "Ngưỡng tổng tiền sách được miễn phí vận chuyển (VND)", example = "300000")
    @NotNull(message = "Ngưỡng miễn phí ship không được để trống")
    @Min(value = 0, message = "Ngưỡng miễn phí ship không được âm")
    private Long freeShippingThreshold;

    @Schema(description = "Mức hỗ trợ tối đa khi áp mã freeship (VND)", example = "30000")
    @NotNull(message = "Mức hỗ trợ freeship tối đa không được để trống")
    @Min(value = 0, message = "Mức hỗ trợ freeship không được âm")
    private Long maxFreeShippingSubsidy;

    @Schema(description = "Định mức cân nặng cơ bản của kiện hàng (gram)", example = "2000")
    @NotNull(message = "Trọng lượng cơ bản không được để trống")
    @Min(value = 100, message = "Trọng lượng cơ bản phải từ 100g trở lên")
    private Integer standardMaxWeightGram;

    @Schema(description = "Đơn vị tính vượt cân (gram)", example = "500")
    @NotNull(message = "Đơn vị tính vượt cân không được để trống")
    @Min(value = 50, message = "Đơn vị tính vượt cân phải từ 50g trở lên")
    private Integer overweightUnitGram;

    @Schema(description = "Phụ phí vượt cân trên mỗi đơn vị cân nặng (VND)", example = "5000")
    @NotNull(message = "Phụ phí vượt cân không được để trống")
    @Min(value = 0, message = "Phụ phí vượt cân không được âm")
    private Long overweightSurcharge;
}

