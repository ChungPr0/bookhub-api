package com.chungpr0.bookhub.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Yêu cầu cập nhật thông tin nhà xuất bản")
public class UpdatePublisherRequest {

    @NotBlank(message = "Tên nhà xuất bản không được để trống")
    @Size(min = 2, max = 150, message = "Tên nhà xuất bản phải từ 2 đến 150 ký tự")
    @Schema(description = "Tên đầy đủ của nhà xuất bản", example = "NXB Trẻ")
    private String name;

    @Size(max = 255, message = "Địa chỉ trụ sở tối đa 255 ký tự")
    @Schema(description = "Địa chỉ trụ sở NXB", example = "161B Lý Chính Thắng, P. Võ Thị Sáu, Q.3, TP.HCM")
    private String address;

    @Size(max = 255, message = "Địa chỉ website tối đa 255 ký tự")
    @Schema(description = "Trang web chính thức của NXB", example = "https://www.nxbtre.com.vn")
    private String website;
}

