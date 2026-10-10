package com.chungpr0.bookhub.modules.user.dto.request;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
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
public class StaffFilterRequest {

    @Schema(description = "Từ khóa tìm kiếm theo họ tên, email hoặc số điện thoại", example = "Khoa")
    private String keyword;

    @Schema(description = "Lọc theo vai trò nội bộ", example = "STAFF")
    private Role role;

    @Schema(description = "Lọc theo trạng thái tài khoản", example = "ACTIVE")
    private AccountStatus status;
}

