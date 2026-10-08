package com.chungpr0.bookhub.modules.order.dto.request;

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
public class RecreatePaymentUrlRequest {

    @Schema(description = "Mã phương thức ngân hàng liên kết VNPAY (VNPAYQR, VNBANK, INTCARD)", example = "VNPAYQR")
    private String bankCode;
}

