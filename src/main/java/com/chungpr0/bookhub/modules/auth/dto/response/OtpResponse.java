package com.chungpr0.bookhub.modules.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {

    @Schema(description = "Số điện thoại nhận mã OTP (đã che giấu)", example = "098****888")
    private String phone;

    @Schema(description = "Thời gian hiệu lực của mã OTP tính theo giây (300s = 5 phút)", example = "300")
    private long otpExpiresInSeconds;

    @Schema(description = "Thời gian cần chờ trước khi gửi lại OTP tiếp theo tính theo giây", example = "60")
    private long resendAfterSeconds;
}

