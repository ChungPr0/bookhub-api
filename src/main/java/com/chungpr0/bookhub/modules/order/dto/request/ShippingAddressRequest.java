package com.chungpr0.bookhub.modules.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ShippingAddressRequest {

    @Schema(description = "Tên người nhận hàng", example = "Nguyễn Tiến Chung")
    @NotBlank(message = "Tên người nhận không được để trống")
    private String fullName;

    @Schema(description = "Số điện thoại người nhận hàng (10 số)", example = "0988888888")
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
    private String phone;

    @Schema(description = "Tỉnh / Thành phố", example = "Thành phố Hà Nội")
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String province;

    @Schema(description = "Quận / Huyện", example = "Quận Cầu Giấy")
    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;

    @Schema(description = "Phường / Xã", example = "Phường Dịch Vọng")
    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    @Schema(description = "Địa chỉ chi tiết (số nhà, ngõ, tên đường)", example = "Số 10, Ngõ 2, Trần Thái Tông")
    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String detailAddress;

    public String toFullAddressString() {
        return String.format("%s, %s, %s, %s", detailAddress, ward, district, province);
    }
}

