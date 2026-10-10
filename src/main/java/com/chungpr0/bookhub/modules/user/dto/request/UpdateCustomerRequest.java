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
public class UpdateCustomerRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Tiến Chung")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    @Schema(description = "Địa chỉ email", example = "tienchung.dev@gmail.com")
    private String email;

    @NotNull(message = "Giới tính không được để trống")
    @Schema(description = "Giới tính", example = "MALE")
    private Gender gender;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @Schema(description = "Ngày sinh", example = "2002-04-03")
    private LocalDate birthday;
}

