package com.chungpr0.bookhub.modules.catalog.dto.response;

import com.chungpr0.bookhub.common.enums.CustomerTier;
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
@Schema(description = "Thông tin khách hàng đánh giá (tên đã được ẩn bảo mật)")
public class ReviewCustomerResponse {

    @Schema(description = "Tên hiển thị đã được mask bảo mật", example = "Nguyễn T. C.")
    private String displayName;

    @Schema(description = "Ảnh đại diện", example = "https://cdn.bookhub.vn/avatars/user-1001.webp")
    private String avatarUrl;

    @Schema(description = "Hạng thành viên", example = "SILVER")
    private CustomerTier tier;
}

