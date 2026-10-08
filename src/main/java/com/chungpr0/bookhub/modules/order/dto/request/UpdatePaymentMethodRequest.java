package com.chungpr0.bookhub.modules.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
public class UpdatePaymentMethodRequest {

    @Schema(description = "Tên hiển thị phương thức thanh toán", example = "Chuyển khoản QR ngân hàng")
    @Size(max = 100, message = "Tên phương thức không được vượt quá 100 ký tự")
    private String name;

    @Schema(description = "Mô tả chi tiết cách thanh toán", example = "Quét mã QR qua app ngân hàng 24/7")
    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    private String description;

    @Schema(description = "Bật / tắt hoạt động của phương thức thanh toán", example = "true")
    private Boolean isActive;

    @Schema(description = "Tên ngân hàng thụ hưởng", example = "Ngân hàng Thương mại Cổ phần Ngoại thương Việt Nam (Vietcombank)")
    private String bankName;

    @Schema(description = "Số tài khoản ngân hàng thụ hưởng", example = "0011004567890")
    private String accountNumber;

    @Schema(description = "Tên chủ tài khoản thụ hưởng", example = "NHA SACH BOOKHUB VIET NAM")
    private String accountName;

    @Schema(description = "Cú pháp nội dung chuyển khoản mẫu", example = "BOOKHUB {orderCode}")
    private String transferContentTemplate;
}

