package com.chungpr0.bookhub.modules.cart.dto.response;

import com.chungpr0.bookhub.common.enums.StockStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin cuốn sách trong danh sách yêu thích")
public class WishlistItemResponse {

    @Schema(description = "ID sách", example = "101")
    private Long id;

    @Schema(description = "Tên cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Slug cuốn sách", example = "nha-gia-kim")
    private String slug;

    @Schema(description = "Ảnh bìa đại diện", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;

    @Schema(description = "Danh sách tác giả")
    @Builder.Default
    private List<CartItemAuthorResponse> authors = new ArrayList<>();

    @Schema(description = "Giá gốc (VND)", example = "79000")
    private Long originalPrice;

    @Schema(description = "Giá bán hiện tại (VND)", example = "63200")
    private Long salePrice;

    @Schema(description = "Tỷ lệ giảm giá (%)", example = "20")
    private Integer discountPercent;

    @Schema(description = "Điểm đánh giá trung bình", example = "4.7")
    private Double avgRating;

    @Schema(description = "Số lượng đánh giá", example = "1284")
    private Integer reviewCount;

    @Schema(description = "Trạng thái tồn kho", example = "IN_STOCK")
    private StockStatus stockStatus;

    @JsonProperty("isBestSeller")
    @Schema(description = "Có phải sách bán chạy hay không", example = "true")
    private Boolean isBestSeller;

    @Schema(description = "Thời điểm thêm vào danh sách yêu thích", example = "2026-10-05T14:00:00+07:00")
    private OffsetDateTime addedAt;
}

