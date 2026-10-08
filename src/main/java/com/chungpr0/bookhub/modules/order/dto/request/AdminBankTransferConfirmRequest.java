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

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBankTransferConfirmRequest {

    @Schema(description = "Số tiền thực tế nhận được trong sao kê tài khoản ngân hàng", example = "258760")
    @NotNull(message = "Số tiền nhận được không được để trống")
    @Min(value = 0, message = "Số tiền nhận được không được âm")
    private Long amountReceived;

    @Schema(description = "Mã tham chiếu / mã giao dịch ngân hàng sao kê", example = "FT2628091245678")
    @NotBlank(message = "Mã giao dịch ngân hàng không được để trống")
    private String bankTransactionRef;

    @Schema(description = "Thời gian ghi nhận giao dịch thành công (nếu không truyền mặc định thời điểm hiện tại)")
    private OffsetDateTime paidAt;

    @Schema(description = "Ghi chú kế toán", example = "Khớp giao dịch Vietcombank sao kê lúc 18h")
    private String note;
}

