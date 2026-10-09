package com.chungpr0.bookhub.modules.catalog.dto.response;

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
@Schema(description = "Thông tin khách hàng viết đánh giá (phục vụ Quản trị viên)")
public class ReviewCustomerInfoResponse {

    @Schema(description = "ID khách hàng", example = "1001")
    private Long id;

    @Schema(description = "Họ và tên khách hàng", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Số điện thoại khách hàng", example = "0988888888")
    private String phone;

    @Schema(description = "URL ảnh đại diện", example = "https://cdn.bookhub.vn/avatars/user-1001.webp")
    private String avatarUrl;
}

