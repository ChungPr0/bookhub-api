package com.chungpr0.bookhub.modules.user.dto.response;

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
public class CreateStaffResponse {

    @Schema(description = "ID nhân viên", example = "15")
    private Long id;

    @Schema(description = "Số điện thoại tài khoản", example = "0911223344")
    private String phone;

    @Schema(description = "Họ và tên nhân viên", example = "Lê Văn Khoa")
    private String fullName;

    @Schema(description = "Vai trò nội bộ được gán", example = "STAFF")
    private Role role;

    @Schema(description = "Trạng thái tài khoản ban đầu", example = "UNVERIFIED")
    private AccountStatus status;

    @Schema(description = "Mật khẩu tạm thời một lần (cần đổi khi đăng nhập lần đầu)", example = "Xk9#mP2$vL8q")
    private String temporaryPassword;
}

