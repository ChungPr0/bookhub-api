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
public class RefreshTokenResponse {

    @Schema(description = "Access Token (JWT) mới", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh Token mới (theo quy chuẩn Refresh Token Rotation)", example = "rt_1122334455667788")
    private String refreshToken;

    @Schema(description = "Loại token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Thời gian hết hạn của Access Token (giây)", example = "1800")
    private long expiresIn;

    @Schema(description = "Thời gian hết hạn của Refresh Token (giây)", example = "604800")
    private long refreshExpiresIn;
}

