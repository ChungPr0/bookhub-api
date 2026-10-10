package com.chungpr0.bookhub.modules.user.dto.response;

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
public class ResetStaffPasswordResponse {

    @Schema(description = "Số điện thoại tài khoản nhân viên", example = "0911223344")
    private String phone;

    @Schema(description = "Mật khẩu tạm thời một lần vừa cấp lại", example = "Tg7!rW4@bK1p")
    private String temporaryPassword;
}

