package com.chungpr0.bookhub.modules.inventory.dto.response;

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
@Schema(description = "Thông tin tóm tắt cuốn sách")
public class SimpleBookResponse {

    @Schema(description = "ID cuốn sách", example = "101")
    private Long id;

    @Schema(description = "Mã ISBN", example = "9786045629870")
    private String isbn;

    @Schema(description = "Tên cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Đường dẫn định danh (Slug)", example = "nha-gia-kim")
    private String slug;

    @Schema(description = "Ảnh đại diện bìa sách", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;
}

