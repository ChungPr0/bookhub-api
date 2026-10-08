package com.chungpr0.bookhub.modules.auth.dto.response;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    @Schema(description = "ID tài khoản", example = "1")
    private Long accountId;

    @Schema(description = "Số điện thoại tài khoản", example = "0988888888")
    private String phone;

    @Schema(description = "Vai trò người dùng trong hệ thống", example = "CUSTOMER")
    private Role role;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Cờ bắt buộc đổi mật khẩu", example = "false")
    private boolean mustChangePassword;

    @Schema(description = "Họ và tên người dùng", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Email người dùng", example = "nguyenvana@gmail.com")
    private String email;

    @Schema(description = "Đường dẫn ảnh đại diện", example = "https://cdn.bookhub.com/avatars/user1.png")
    private String avatarUrl;

    @Schema(description = "Danh sách quyền hạn của vai trò")
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
}

