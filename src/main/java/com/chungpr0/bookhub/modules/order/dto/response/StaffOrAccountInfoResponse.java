package com.chungpr0.bookhub.modules.order.dto.response;

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
public class StaffOrAccountInfoResponse {

    @Schema(description = "ID tài khoản nhân viên", example = "101")
    private Long id;

    @Schema(description = "Tên nhân viên thao tác", example = "Trần Thị Thu")
    private String fullName;

    @Schema(description = "Vai trò (STAFF, MANAGER, ADMIN)", example = "STAFF")
    private String role;
}

