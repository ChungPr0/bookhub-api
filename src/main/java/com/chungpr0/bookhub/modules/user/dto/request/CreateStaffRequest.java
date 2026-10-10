package com.chungpr0.bookhub.modules.user.dto.request;

import com.chungpr0.bookhub.common.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class CreateStaffRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    @Schema(description = "Số điện thoại nhân sự", example = "0911223344")
    private String phone;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    @Schema(description = "Họ và tên nhân viên", example = "Lê Văn Khoa")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    @Schema(description = "Địa chỉ email nhân viên", example = "khoale@bookhub.vn")
    private String email;

    @NotNull(message = "Vai trò không được để trống")
    @Schema(description = "Vai trò nội bộ (ADMIN, MANAGER, STAFF)", example = "STAFF")
    private Role role;
}

