package com.chungpr0.bookhub.modules.catalog.dto.request;

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
@Schema(description = "Bộ lọc tìm kiếm sách đa tiêu chí")
public class BookSearchFilter {

    @Schema(description = "Từ khóa tìm kiếm (tiêu đề, ISBN hoặc tên tác giả)", example = "Nhà Giả Kim")
    private String keyword;

    @Schema(description = "Slug danh mục sách", example = "van-hoc")
    private String categorySlug;

    @Schema(description = "Slug tác giả", example = "paulo-coelho")
    private String authorSlug;

    @Schema(description = "Slug nhà xuất bản", example = "nxb-hoi-nha-van")
    private String publisherSlug;

    @Schema(description = "Ngôn ngữ xuất bản (VD: VI, EN)", example = "VI")
    private String language;

    @Schema(description = "Hình thức bìa (PAPERBACK hoặc HARDCOVER)", example = "PAPERBACK")
    private String coverType;

    @Schema(description = "Mức giá tối thiểu (VND)", example = "50000")
    private Long priceFrom;

    @Schema(description = "Mức giá tối đa (VND)", example = "200000")
    private Long priceTo;

    @Schema(description = "Đánh giá tối thiểu từ 1 đến 5 sao", example = "4")
    private Integer minRating;

    @Schema(description = "Chỉ lấy sách còn hàng", example = "false")
    @Builder.Default
    private Boolean inStockOnly = false;
}

