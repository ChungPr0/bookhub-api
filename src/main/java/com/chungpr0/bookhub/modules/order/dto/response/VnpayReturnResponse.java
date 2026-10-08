package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
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
public class VnpayReturnResponse {

    @Schema(description = "Mã đơn hàng", example = "ORD-20261007-K7X9QM")
    private String orderCode;

    @Schema(description = "Kết quả giao dịch (SUCCESS hoặc FAILED)", example = "SUCCESS")
    private String paymentResult;

    @Schema(description = "Trạng thái thanh toán của đơn hàng", example = "PAID")
    private PaymentStatus paymentStatus;

    @Schema(description = "Thông báo kết quả cho người dùng", example = "Thanh toán thành công qua VNPAY")
    private String message;

    @Schema(description = "Mã phản hồi từ cổng VNPAY", example = "00")
    private String vnpResponseCode;
}

