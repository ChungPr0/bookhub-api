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
public class ChangePasswordRequest {

    @Schema(description = "Mật khẩu hiện tại của tài khoản", example = "OldPass123")
    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    private String currentPassword;

    @Schema(description = "Mật khẩu mới (8-64 ký tự, có chữ hoa, thường và số)", example = "NewPass123")
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,64}$",
            message = "Mật khẩu mới phải có từ 8-64 ký tự, gồm chữ hoa, chữ thường và chữ số, không chứa khoảng trắng"
    )
    private String newPassword;
}

