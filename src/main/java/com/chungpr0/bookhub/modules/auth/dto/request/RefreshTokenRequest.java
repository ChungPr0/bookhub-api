package com.chungpr0.bookhub.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @Schema(description = "Mã Refresh Token dạng opaque token đã nhận khi đăng nhập", example = "rt_550e8400e29b41d4a716446655440000a1b2c3d4e5f6")
    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}

