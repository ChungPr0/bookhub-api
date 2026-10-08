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
@Schema(description = "Thẻ tóm tắt nhà xuất bản trên danh sách NXB")
public class PublisherCardResponse {

    @Schema(description = "ID nhà xuất bản", example = "4")
    private Long id;

    @Schema(description = "Tên nhà xuất bản", example = "NXB Hội Nhà Văn")
    private String name;

    @Schema(description = "Slug nhà xuất bản", example = "nxb-hoi-nha-van")
    private String slug;

    @Schema(description = "Địa chỉ trụ sở", example = "65 Nguyễn Du, Hai Bà Trưng, Hà Nội")
    private String address;

    @Schema(description = "Website chính thức", example = "https://nxbhoinhavan.vn")
    private String website;

    @Schema(description = "Số lượng tác phẩm", example = "35")
    private int bookCount;
}

