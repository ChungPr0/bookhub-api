package com.chungpr0.bookhub.modules.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopBookReportResponse {

    @Schema(description = "Thứ hạng sách bán chạy", example = "1")
    private int rank;

    @Schema(description = "Thông tin chi tiết về cuốn sách")
    private BookInfo book;

    @Schema(description = "Số lượng cuốn đã bán", example = "540")
    private int quantitySold;

    @Schema(description = "Doanh thu mang lại (VNĐ)", example = "42660000")
    private Long revenue;

    @Schema(description = "Giá vốn hàng bán (VNĐ)", example = "23760000")
    private Long costOfGoods;

    @Schema(description = "Lợi nhuận gộp tạo ra (VNĐ)", example = "18900000")
    private Long profit;

    @Schema(description = "Số lượng tồn kho hiện tại", example = "85")
    private int currentStock;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        @Schema(description = "ID sách", example = "1")
        private Long id;

        @Schema(description = "Mã ISBN của sách", example = "9786045892345")
        private String isbn;

        @Schema(description = "Tựa đề cuốn sách", example = "Nhà Giả Kim")
        private String title;

        @Schema(description = "Ảnh bìa sách", example = "https://cdn.bookhub.vn/books/nha-gia-kim-cover.webp")
        private String thumbnailUrl;

        @Schema(description = "Danh sách tác giả của sách")
        private List<AuthorInfo> authors;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        @Schema(description = "ID tác giả", example = "1")
        private Long id;

        @Schema(description = "Tên tác giả", example = "Paulo Coelho")
        private String name;
    }
}

