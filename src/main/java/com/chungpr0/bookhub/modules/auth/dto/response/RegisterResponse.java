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
public class RegisterResponse {

    @Schema(description = "Số điện thoại tài khoản đăng ký", example = "0988888888")
    private String phone;

    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Văn A")
    private String fullName;
}

