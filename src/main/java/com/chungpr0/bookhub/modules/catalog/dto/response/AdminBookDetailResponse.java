package com.chungpr0.bookhub.modules.catalog.dto.response;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
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
@Schema(description = "Chi tiết kỹ thuật sách phục vụ quản trị viên")
public class AdminBookDetailResponse {

    @Schema(description = "ID sách", example = "101")
    private Long id;

    @Schema(description = "Mã ISBN", example = "9786045629870")
    private String isbn;

    @Schema(description = "Tiêu đề cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Slug cuốn sách", example = "nha-gia-kim")
    private String slug;

    @Schema(description = "Giá gốc (VND)", example = "79000")
    private Long originalPrice;

    @Schema(description = "Giá bán thực tế (VND)", example = "63200")
    private Long salePrice;

    @Schema(description = "Số lượng tồn kho thực tế", example = "150")
    private int stockQuantity;

    @Schema(description = "Ngưỡng cảnh báo tồn kho thấp", example = "10")
    private int lowStockThreshold;

    @Schema(description = "Trạng thái kinh doanh", example = "ACTIVE")
    private BookStatus status;

    @Schema(description = "Danh sách tác giả")
    @Builder.Default
    private List<AuthorSummaryResponse> authors = new ArrayList<>();

    @Schema(description = "Tên dịch giả", example = "Lê Chu Cầu")
    private String translator;

    @Schema(description = "Nhà xuất bản")
    private PublisherSummaryResponse publisher;

    @Schema(description = "Danh mục trực tiếp")
    private CategorySummaryResponse category;

    @Schema(description = "Năm xuất bản", example = "2020")
    private Integer publicationYear;

    @Schema(description = "Ngôn ngữ", example = "VI")
    private String language;

    @Schema(description = "Số trang", example = "228")
    private Integer pageCount;

    @Schema(description = "Hình thức bìa", example = "PAPERBACK")
    private CoverType coverType;

    @Schema(description = "Kích thước cuốn sách", example = "13 x 20.5 cm")
    private String dimensions;

    @Schema(description = "Khối lượng sách tính theo gram", example = "260")
    private Integer weightGram;

    @Schema(description = "Danh sách hình ảnh")
    @Builder.Default
    private List<BookImageResponse> images = new ArrayList<>();

    @Schema(description = "Mô tả nội dung")
    private String description;

    @Schema(description = "Số lượng đã bán", example = "5320")
    private int soldCount;

    @Schema(description = "Phiên bản optimistic locking", example = "0")
    private int version;

    @Schema(description = "Thời gian tạo", example = "2026-01-10T08:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-10-08T18:00:00+07:00")
    private OffsetDateTime updatedAt;
}

