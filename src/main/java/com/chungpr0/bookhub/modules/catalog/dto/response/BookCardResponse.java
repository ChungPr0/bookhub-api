package com.chungpr0.bookhub.modules.catalog.dto.response;

import com.chungpr0.bookhub.common.enums.StockStatus;
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
@Schema(description = "Thẻ sách hiển thị trên danh sách, danh mục và trang chủ")
public class BookCardResponse {

    @Schema(description = "ID sách", example = "101")
    private Long id;

    @Schema(description = "Tiêu đề cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Slug cuốn sách", example = "nha-gia-kim")
    private String slug;

    @Schema(description = "URL ảnh bìa đại diện", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;

    @Schema(description = "Danh sách tác giả")
    @Builder.Default
    private List<AuthorSummaryResponse> authors = new ArrayList<>();

    @Schema(description = "Giá gốc (VND)", example = "79000")
    private Long originalPrice;

    @Schema(description = "Giá bán khuyến mãi thực tế (VND)", example = "63200")
    private Long salePrice;

    @Schema(description = "Phần trăm giảm giá (%)", example = "20")
    private int discountPercent;

    @Schema(description = "Điểm đánh giá trung bình", example = "4.7")
    private Double avgRating;

    @Schema(description = "Số lượng đánh giá", example = "1284")
    private int reviewCount;

    @Schema(description = "Số lượng đã bán", example = "5320")
    private int soldCount;

    @Schema(description = "Trạng thái tồn kho an toàn", example = "IN_STOCK")
    private StockStatus stockStatus;

    @com.fasterxml.jackson.annotation.JsonProperty("isBestSeller")
    @Schema(description = "Đánh dấu sách bán chạy", example = "true")
    private boolean isBestSeller;
}

