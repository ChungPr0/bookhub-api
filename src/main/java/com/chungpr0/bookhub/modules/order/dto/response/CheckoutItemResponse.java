package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.common.enums.CartItemAvailability;
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
public class CheckoutItemResponse {

    @Schema(description = "ID cuốn sách", example = "101")
    private Long bookId;

    @Schema(description = "Tên cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Ảnh bìa cuốn sách", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;

    @Schema(description = "Đơn giá hiện tại (VND)", example = "63200")
    private Long unitPrice;

    @Schema(description = "Số lượng mua", example = "2")
    private Integer quantity;

    @Schema(description = "Thành tiền của sách (unitPrice * quantity)", example = "126400")
    private Long lineTotal;

    @Schema(description = "Trạng thái khả dụng của sách", example = "AVAILABLE")
    private CartItemAvailability availability;
}

