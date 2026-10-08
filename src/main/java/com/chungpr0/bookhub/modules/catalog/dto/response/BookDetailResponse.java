package com.chungpr0.bookhub.modules.catalog.dto.response;

import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.StockStatus;
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
@Schema(description = "Chi tiết đầy đủ thông tin sách cho người dùng")
public class BookDetailResponse {

    @Schema(description = "ID sách", example = "101")
    private Long id;

    @Schema(description = "Mã chuẩn hóa ISBN", example = "9786045629870")
    private String isbn;

    @Schema(description = "Tiêu đề cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Slug cuốn sách", example = "nha-gia-kim")
    private String slug;

    @Schema(description = "Giá gốc (VND)", example = "79000")
    private Long originalPrice;

    @Schema(description = "Giá bán thực tế (VND)", example = "63200")
    private Long salePrice;

    @Schema(description = "Phần trăm giảm giá (%)", example = "20")
    private int discountPercent;

    @Schema(description = "Trạng thái tồn kho an toàn", example = "IN_STOCK")
    private StockStatus stockStatus;

    @Schema(description = "Số lượng tối đa có thể mua trong một đơn", example = "84")
    private int maxPurchasableQuantity;

    @com.fasterxml.jackson.annotation.JsonProperty("isBestSeller")
    @Schema(description = "Đánh dấu sách bán chạy", example = "true")
    private boolean isBestSeller;

    @com.fasterxml.jackson.annotation.JsonProperty("isInWishlist")
    @Schema(description = "Trạng thái nằm trong danh sách yêu thích của người dùng", example = "false")
    private boolean isInWishlist;

    @Schema(description = "Danh sách tác giả")
    @Builder.Default
    private List<AuthorSummaryResponse> authors = new ArrayList<>();

    @Schema(description = "Tên dịch giả", example = "Lê Chu Cầu")
    private String translator;

    @Schema(description = "Nhà xuất bản")
    private PublisherSummaryResponse publisher;

    @Schema(description = "Danh mục trực tiếp")
    private CategorySummaryResponse category;

    @Schema(description = "Chuỗi điều hướng breadcrumb")
    @Builder.Default
    private List<CategoryBreadcrumbItem> breadcrumb = new ArrayList<>();

    @Schema(description = "Năm xuất bản", example = "2020")
    private Integer publicationYear;

    @Schema(description = "Mã ngôn ngữ", example = "VI")
    private String language;

    @Schema(description = "Số trang", example = "228")
    private Integer pageCount;

    @Schema(description = "Hình thức bìa", example = "PAPERBACK")
    private CoverType coverType;

    @Schema(description = "Kích thước cuốn sách", example = "13 x 20.5 cm")
    private String dimensions;

    @Schema(description = "Khối lượng sách tính theo gram", example = "260")
    private Integer weightGram;

    @Schema(description = "Danh sách hình ảnh minh họa")
    @Builder.Default
    private List<BookImageResponse> images = new ArrayList<>();

    @Schema(description = "Bài viết mô tả nội dung sách (HTML đã sanitize)")
    private String description;

    @Schema(description = "Thống kê điểm đánh giá và sao")
    private RatingSummaryResponse ratingSummary;

    @Schema(description = "Số lượng sách đã bán", example = "5320")
    private int soldCount;

    @Schema(description = "Thời gian tạo sản phẩm", example = "2026-01-10T08:00:00+07:00")
    private OffsetDateTime createdAt;
}

