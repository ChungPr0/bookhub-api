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
public class BankInfoResponse {

    @Schema(description = "Tên ngân hàng thụ hưởng", example = "Ngân hàng Thương mại Cổ phần Ngoại thương Việt Nam (Vietcombank)")
    private String bankName;

    @Schema(description = "Số tài khoản ngân hàng", example = "0011004567890")
    private String accountNumber;

    @Schema(description = "Tên chủ tài khoản", example = "NHA SACH BOOKHUB VIET NAM")
    private String accountName;

    @Schema(description = "Cú pháp chuyển khoản mẫu", example = "BOOKHUB {orderCode}")
    private String transferContentTemplate;
}

