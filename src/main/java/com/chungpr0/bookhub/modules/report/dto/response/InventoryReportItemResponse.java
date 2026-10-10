package com.chungpr0.bookhub.modules.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportItemResponse {

    @Schema(description = "Thông tin cuốn sách")
    private SimpleBookInfo book;

    @Schema(description = "Số lượng tồn kho hiện tại", example = "85")
    private int stockQuantity;

    @Schema(description = "Tổng giá trị vốn tồn kho theo FIFO (VNĐ)", example = "3740000")
    private Long stockValue;

    @Schema(description = "Tổng giá trị bán lẻ dự kiến (VNĐ)", example = "6715000")
    private Long retailValue;

    @Schema(description = "Giá vốn nhập trung bình của các lô còn tồn (VNĐ)", example = "44000")
    private Long avgImportPrice;

    @Schema(description = "Số lượng cuốn đã bán trong 90 ngày gần nhất", example = "620")
    private int soldLast90Days;

    @Schema(description = "Thời điểm phát sinh đơn bán gần nhất", example = "2026-10-07T14:20:00+07:00")
    private OffsetDateTime lastSoldAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleBookInfo {
        @Schema(description = "ID sách", example = "1")
        private Long id;

        @Schema(description = "Mã ISBN", example = "9786045892345")
        private String isbn;

        @Schema(description = "Tựa đề cuốn sách", example = "Nhà Giả Kim")
        private String title;

        @Schema(description = "Ảnh bìa cuốn sách", example = "https://cdn.bookhub.vn/books/nha-gia-kim-cover.webp")
        private String thumbnailUrl;
    }
}

