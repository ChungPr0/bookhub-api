package com.chungpr0.bookhub.modules.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResetRequest {

    @Schema(description = "Số điện thoại tài khoản cần đặt lại mật khẩu", example = "0988888888")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String phone;

    @Schema(description = "Token xác thực đặt lại mật khẩu nhận được từ bước verify OTP", example = "prt_6ba7b8109dad11d180b400c04fd430c8")
    @NotBlank(message = "Mã xác thực đặt lại mật khẩu không được để trống")
    private String resetToken;

    @Schema(description = "Mật khẩu mới (8-64 ký tự, có chữ hoa, thường và số)", example = "NewSecurePass123")
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,64}$",
            message = "Mật khẩu mới phải có từ 8-64 ký tự, gồm chữ hoa, chữ thường và chữ số, không chứa khoảng trắng"
    )
    private String newPassword;
}

