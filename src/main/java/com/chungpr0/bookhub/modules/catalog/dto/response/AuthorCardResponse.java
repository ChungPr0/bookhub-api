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
@Schema(description = "Thẻ tóm tắt tác giả trên danh sách tác giả")
public class AuthorCardResponse {

    @Schema(description = "ID tác giả", example = "7")
    private Long id;

    @Schema(description = "Tên tác giả", example = "Paulo Coelho")
    private String name;

    @Schema(description = "Slug tác giả", example = "paulo-coelho")
    private String slug;

    @Schema(description = "URL ảnh chân dung đại diện", example = "https://cdn.bookhub.vn/authors/paulo-coelho.webp")
    private String avatarUrl;

    @Schema(description = "Số lượng tác phẩm đang mở bán", example = "12")
    private int bookCount;
}

