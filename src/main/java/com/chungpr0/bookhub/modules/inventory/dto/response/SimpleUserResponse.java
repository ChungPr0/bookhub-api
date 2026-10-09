package com.chungpr0.bookhub.modules.inventory.dto.response;

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
@Schema(description = "Thông tin tóm tắt người dùng / nhân viên thực hiện")
public class SimpleUserResponse {

    @Schema(description = "ID tài khoản", example = "1")
    private Long id;

    @Schema(description = "Họ tên người thực hiện", example = "Trần Thị Thu")
    private String fullName;
}

