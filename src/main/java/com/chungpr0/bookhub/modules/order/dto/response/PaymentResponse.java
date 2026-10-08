package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentTxnStatus;
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
public class PaymentResponse {

    @Schema(description = "ID bản ghi thanh toán", example = "301")
    private Long id;

    @Schema(description = "Phương thức giao dịch (COD, BANK_TRANSFER, VNPAY)", example = "VNPAY")
    private PaymentMethodCode method;

    @Schema(description = "Số tiền thanh toán (VND)", example = "156400")
    private Long amount;

    @Schema(description = "Trạng thái giao dịch (PENDING, SUCCESS, FAILED, REFUNDED)", example = "SUCCESS")
    private PaymentTxnStatus status;

    @Schema(description = "Mã tham chiếu gửi đối tác cổng / hệ thống", example = "TXN-20261007-8801")
    private String transactionRef;

    @Schema(description = "Mã giao dịch từ phía nhà cung cấp cổng thanh toán", example = "14567890")
    private String providerTransactionNo;

    @Schema(description = "Mã ngân hàng thực hiện thanh toán", example = "NCB")
    private String bankCode;

    @Schema(description = "Thời gian hoàn tất giao dịch")
    private OffsetDateTime paidAt;
}

