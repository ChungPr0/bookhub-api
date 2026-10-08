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
public class LoginRequest {

    @Schema(description = "Số điện thoại đăng nhập (10 chữ số)", example = "0988888888")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String phone;

    @Schema(description = "Mật khẩu tài khoản", example = "Password123")
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}

