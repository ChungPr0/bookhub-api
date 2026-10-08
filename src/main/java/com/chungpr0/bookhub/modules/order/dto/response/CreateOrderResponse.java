package com.chungpr0.bookhub.modules.order.dto.response;

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
public class CreateOrderResponse {

    @Schema(description = "Thông tin chi tiết đơn hàng vừa tạo")
    private CustomerOrderDetailResponse order;

    @Schema(description = "Đường link cổng thanh toán VNPAY (chỉ có khi chọn VNPAY)", example = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?...")
    private String paymentUrl;

    @Schema(description = "Hướng dẫn chuyển khoản ngân hàng (chỉ có khi chọn BANK_TRANSFER)")
    private BankTransferInstructionResponse bankTransferInstruction;
}

