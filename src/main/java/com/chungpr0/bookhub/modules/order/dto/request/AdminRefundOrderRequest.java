package com.chungpr0.bookhub.modules.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class AdminRefundOrderRequest {

    @Schema(description = "Số tiền đã hoàn lại cho khách hàng (VND)", example = "258760")
    @NotNull(message = "Số tiền hoàn lại không được để trống")
    @Min(value = 0, message = "Số tiền hoàn lại không được âm")
    private Long refundAmount;

    @Schema(description = "Phương thức hoàn tiền (BANK_TRANSFER, VNPAY)", example = "BANK_TRANSFER")
    @NotBlank(message = "Phương thức hoàn tiền không được để trống")
    private String refundMethod;

    @Schema(description = "Mã giao dịch hoàn tiền", example = "REFUND-VCB-998811")
    @NotBlank(message = "Mã giao dịch hoàn tiền không được để trống")
    private String refundTransactionRef;

    @Schema(description = "Ghi chú tài chính hoàn tiền", example = "Đã hoàn tiền về STK Vietcombank của khách hàng")
    private String note;
}

