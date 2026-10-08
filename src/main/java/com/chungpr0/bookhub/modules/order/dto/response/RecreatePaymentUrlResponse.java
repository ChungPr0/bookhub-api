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
public class RecreatePaymentUrlResponse {

    @Schema(description = "Đường dẫn cổng thanh toán VNPAY", example = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...")
    private String paymentUrl;

    @Schema(description = "Mã tham chiếu giao dịch", example = "TXN-20261007-8801-V2")
    private String transactionRef;

    @Schema(description = "Số tiền thanh toán (VND)", example = "258760")
    private Long amount;

    @Schema(description = "Thời điểm hết hạn thanh toán")
    private OffsetDateTime expiresAt;
}

