package com.chungpr0.bookhub.modules.auth.dto.request;

import com.chungpr0.bookhub.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Schema(description = "Số điện thoại Việt Nam (10 chữ số)", example = "0988888888")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String phone;

    @Schema(description = "Mã xác thực OTP gồm 6 chữ số nhận từ SMS", example = "123456")
    @NotBlank(message = "Mã OTP không được để trống")
    @Pattern(regexp = "^[0-9]{6}$", message = "Mã OTP phải gồm 6 chữ số")
    private String otpCode;

    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Văn A")
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @Schema(description = "Mật khẩu bảo mật (8-64 ký tự, có chữ hoa, thường và số)", example = "Password123")
    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,64}$",
            message = "Mật khẩu phải từ 8-64 ký tự, gồm chữ hoa, chữ thường và chữ số, không chứa khoảng trắng"
    )
    private String password;

    @Schema(description = "Địa chỉ email (tùy chọn)", example = "nguyenvana@gmail.com")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Schema(description = "Giới tính: MALE, FEMALE, OTHER", example = "MALE")
    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @Schema(description = "Ngày tháng năm sinh (yyyy-MM-dd)", example = "2000-01-15")
    @Past(message = "Ngày sinh phải trước ngày hôm nay")
    private LocalDate birthday;
}

