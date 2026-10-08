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
public class LoginResponse {

    @Schema(description = "JWT Access Token dùng để xác thực trong header Authorization: Bearer <token>", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Opaque Refresh Token dùng để xoay vòng và cấp lại Access Token mới", example = "rt_9876543210fedcba")
    private String refreshToken;

    @Schema(description = "Loại token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Thời gian sống của Access Token tính theo giây (1800s = 30 phút)", example = "1800")
    private long expiresIn;

    @Schema(description = "Thời gian sống của Refresh Token tính theo giây (604800s = 7 ngày)", example = "604800")
    private long refreshExpiresIn;

    @Schema(description = "Cờ thông báo tài khoản cần đổi mật khẩu (true nếu là nhân viên mới tạo)", example = "false")
    private boolean mustChangePassword;

    @Schema(description = "Thông tin tóm tắt của người dùng đăng nhập")
    private UserSummaryResponse user;
}

