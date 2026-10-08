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
public class BankTransferInstructionResponse {

    @Schema(description = "Tên ngân hàng thụ hưởng", example = "Vietcombank")
    private String bankName;

    @Schema(description = "Số tài khoản ngân hàng", example = "0011004567890")
    private String accountNumber;

    @Schema(description = "Tên chủ tài khoản", example = "NHA SACH BOOKHUB VIET NAM")
    private String accountName;

    @Schema(description = "Số tiền cần chuyển khoản chính xác (VND)", example = "258760")
    private Long amount;

    @Schema(description = "Nội dung chuyển khoản bắt buộc", example = "BOOKHUB ORD-20261007-K7X9QM")
    private String transferContent;

    @Schema(description = "Đường dẫn mã QR chuyển khoản nhanh (VietQR)", example = "https://img.vietqr.io/image/970436-0011004567890-compact2.png?amount=258760&addInfo=BOOKHUB%20ORD-20261007-K7X9QM")
    private String qrCodeUrl;
}

