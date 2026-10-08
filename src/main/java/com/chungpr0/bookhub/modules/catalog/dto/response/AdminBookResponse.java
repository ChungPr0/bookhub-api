package com.chungpr0.bookhub.modules.catalog.dto.response;

import com.chungpr0.bookhub.common.enums.BookStatus;
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
@Schema(description = "Thông tin quản trị sách trong danh sách quản lý")
public class AdminBookResponse {

    @Schema(description = "ID sách", example = "101")
    private Long id;

    @Schema(description = "Mã ISBN", example = "9786045629870")
    private String isbn;

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

    @Schema(description = "Giá bán thực tế (VND)", example = "63200")
    private Long salePrice;

    @Schema(description = "Tồn kho thực tế trong kho", example = "150")
    private int stockQuantity;

    @Schema(description = "Trạng thái kinh doanh", example = "ACTIVE")
    private BookStatus status;

    @Schema(description = "Số lượng đã bán", example = "5320")
    private int soldCount;

    @Schema(description = "Thời gian tạo", example = "2026-01-10T08:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Phiên bản optimistic locking", example = "0")
    private int version;
}

