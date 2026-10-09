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
@Schema(description = "Thông tin tóm tắt sản phẩm sách gắn liền với đánh giá")
public class BookSummaryItemResponse {

    @Schema(description = "ID cuốn sách", example = "101")
    private Long id;

    @Schema(description = "Tiêu đề cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Đường dẫn thân thiện (slug)", example = "nha-gia-kim")
    private String slug;

    @Schema(description = "URL ảnh bìa thu nhỏ", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;
}

