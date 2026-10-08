package com.chungpr0.bookhub.modules.user.dto.request;

import com.chungpr0.bookhub.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Tiến Chung")
    private String fullName;

    @Email(message = "Email không đúng định dạng RFC 5322")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    @Schema(description = "Địa chỉ email (truyền null hoặc rỗng để xóa)", example = "tienchung.dev@gmail.com")
    private String email;

    @NotNull(message = "Giới tính không được để trống")
    @Schema(description = "Giới tính", example = "MALE")
    private Gender gender;

    @Past(message = "Ngày sinh phải trước ngày hôm nay")
    @Schema(description = "Ngày sinh định dạng YYYY-MM-DD", example = "2002-04-03")
    private LocalDate birthday;

    @Size(max = 500, message = "Đường dẫn ảnh đại diện tối đa 500 ký tự")
    @Schema(description = "Đường dẫn ảnh đại diện hợp lệ từ CDN hoặc null", example = "https://cdn.bookhub.vn/avatars/20261008_avatar.webp")
    private String avatarUrl;
}

