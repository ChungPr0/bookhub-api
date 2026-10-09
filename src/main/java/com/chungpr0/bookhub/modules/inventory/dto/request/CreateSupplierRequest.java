package com.chungpr0.bookhub.modules.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Yêu cầu tạo mới nhà cung cấp")
public class CreateSupplierRequest {

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(min = 2, max = 150, message = "Tên nhà cung cấp từ 2 đến 150 ký tự")
    @Schema(description = "Tên công ty hoặc nhà phân phối sách", example = "Công ty TNHH Sách Alpha (Alpha Books)")
    private String name;

    @Size(max = 100, message = "Tên người liên hệ tối đa 100 ký tự")
    @Schema(description = "Họ tên người liên hệ kinh doanh", example = "Nguyễn Hoàng Anh")
    private String contactName;

    @Pattern(regexp = "^(0|\\+84)[0-9]{8,10}$|^$", message = "Số điện thoại không hợp lệ")
    @Schema(description = "Số điện thoại bàn hoặc di động", example = "02437226234")
    private String phone;

    @Email(message = "Địa chỉ email không đúng định dạng")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    @Schema(description = "Hòm thư liên hệ đặt hàng", example = "contact@alphabooks.vn")
    private String email;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    @Schema(description = "Địa chỉ văn phòng hoặc kho nhà cung cấp", example = "Tầng 3, 11A Thể Giao, Hai Bà Trưng, Hà Nội")
    private String address;

    @Pattern(regexp = "^[0-9]{10}(-[0-9]{3})?$|^$", message = "Mã số thuế phải gồm 10 hoặc 13 chữ số")
    @Schema(description = "Mã số thuế doanh nghiệp", example = "0101678999")
    private String taxCode;
}

