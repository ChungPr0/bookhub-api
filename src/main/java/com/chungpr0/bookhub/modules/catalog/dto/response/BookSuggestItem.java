package com.chungpr0.bookhub.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Mục sách gợi ý nhanh")
public class BookSuggestItem {

    @Schema(description = "ID sách", example = "101")
    private Long id;

    @Schema(description = "Tiêu đề cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Slug cuốn sách", example = "nha-gia-kim")
    private String slug;

    @Schema(description = "Ảnh bìa thumbnail", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;

    @Schema(description = "Giá bán hiện tại (VND)", example = "63200")
    private Long salePrice;

    @Schema(description = "Danh sách tác giả")
    @Builder.Default
    private List<AuthorSummaryResponse> authors = new ArrayList<>();
}

