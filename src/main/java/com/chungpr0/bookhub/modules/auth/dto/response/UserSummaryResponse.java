package com.chungpr0.bookhub.modules.auth.dto.response;

import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    @Schema(description = "ID tài khoản", example = "1")
    private Long accountId;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Số điện thoại", example = "0988888888")
    private String phone;

    @Schema(description = "Vai trò", example = "CUSTOMER")
    private Role role;

    @Schema(description = "Ảnh đại diện", example = "https://cdn.bookhub.com/avatars/user1.png")
    private String avatarUrl;

    @Schema(description = "Hạng thành viên của khách hàng", example = "BRONZE")
    private CustomerTier tier;
}

