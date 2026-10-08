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
public class ResetTokenResponse {

    @Schema(description = "Mã xác thực đặt lại mật khẩu dùng cho bước 3", example = "prt_6ba7b8109dad11d180b400c04fd430c8")
    private String resetToken;

    @Schema(description = "Thời gian hết hạn của resetToken tính theo giây (600s = 10 phút)", example = "600")
    private long expiresInSeconds;
}

