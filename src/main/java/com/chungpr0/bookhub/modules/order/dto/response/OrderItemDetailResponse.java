package com.chungpr0.bookhub.modules.order.dto.response;

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
public class OrderItemDetailResponse {

    @Schema(description = "ID dòng chi tiết đơn hàng", example = "501")
    private Long id;

    @Schema(description = "ID cuốn sách", example = "101")
    private Long bookId;

    @Schema(description = "Tên cuốn sách (snapshot)", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Mã ISBN cuốn sách (snapshot)", example = "9786045629870")
    private String isbn;

    @Schema(description = "Ảnh bìa cuốn sách (snapshot)", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;

    @Schema(description = "Số lượng mua", example = "2")
    private Integer quantity;

    @Schema(description = "Đơn giá mua tại thời điểm chốt đơn (VND)", example = "63200")
    private Long unitPrice;

    @Schema(description = "Thành tiền dòng sách (VND)", example = "126400")
    private Long lineTotal;

    @Schema(description = "Giá vốn xuất kho FIFO (dành cho Admin/Manager/Staff)", example = "94800")
    private Long costAmount;

    @Schema(description = "Lợi nhuận gộp ước tính (dành cho Admin/Manager/Staff)", example = "31600")
    private Long grossProfit;
}

