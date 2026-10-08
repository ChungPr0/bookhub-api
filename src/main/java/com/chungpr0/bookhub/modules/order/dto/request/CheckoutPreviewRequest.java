package com.chungpr0.bookhub.modules.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPreviewRequest {

    @Schema(description = "Danh sách ID sách cần đặt mua", example = "[101, 105]")
    @NotEmpty(message = "Vui lòng chọn ít nhất một cuốn sách để thanh toán")
    private List<Long> bookIds;

    @Schema(description = "Mã voucher giảm giá muốn áp dụng", example = "BOOKHUB10")
    private String voucherCode;

    @Schema(description = "Số điểm thưởng muốn quy đổi giảm giá", example = "200")
    @Min(value = 0, message = "Số điểm thưởng sử dụng không được âm")
    @Builder.Default
    private Integer pointsToUse = 0;

    @Schema(description = "ID địa chỉ giao hàng từ sổ địa chỉ", example = "5")
    private Long addressId;
}

