package com.chungpr0.bookhub.modules.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class AddressRequest {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(min = 2, max = 100, message = "Tên người nhận phải từ 2 đến 100 ký tự")
    @Schema(description = "Tên người nhận hàng", example = "Nguyễn Tiến Chung")
    private String receiverName;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$", message = "Số điện thoại nhận hàng không đúng định dạng Việt Nam")
    @Schema(description = "Số điện thoại nhận hàng", example = "0988888888")
    private String receiverPhone;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh/Thành phố tối đa 100 ký tự")
    @Schema(description = "Tỉnh / Thành phố", example = "Hà Nội")
    private String province;

    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(max = 100, message = "Quận/Huyện tối đa 100 ký tự")
    @Schema(description = "Quận / Huyện", example = "Quận Cầu Giấy")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    @Size(max = 100, message = "Phường/Xã tối đa 100 ký tự")
    @Schema(description = "Phường / Xã", example = "Phường Dịch Vọng")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(min = 5, max = 255, message = "Địa chỉ chi tiết phải từ 5 đến 255 ký tự")
    @Schema(description = "Địa chỉ chi tiết (số nhà, ngõ, tên đường)", example = "Số 10, Ngõ 2, Trần Thái Tông")
    private String detailAddress;

    @com.fasterxml.jackson.annotation.JsonProperty("isDefault")
    @Schema(description = "Đặt làm địa chỉ mặc định hay không", example = "true")
    private Boolean isDefault;
}
