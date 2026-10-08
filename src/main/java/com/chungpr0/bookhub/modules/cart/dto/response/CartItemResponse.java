package com.chungpr0.bookhub.modules.cart.dto.response;

import com.chungpr0.bookhub.common.enums.CartItemAvailability;
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
@Schema(description = "Thông tin chi tiết một dòng sản phẩm trong giỏ hàng")
public class CartItemResponse {

    @Schema(description = "ID sách", example = "101")
    private Long bookId;

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

    @Schema(description = "Số lượng trong giỏ", example = "2")
    private Integer quantity;

    @Schema(description = "Thành tiền của dòng sản phẩm (VND)", example = "126400")
    private Long lineTotal;

    @Schema(description = "Trạng thái tồn kho khả dụng", example = "AVAILABLE")
    private CartItemAvailability availability;

    @Schema(description = "Số lượng tối đa có thể mua được hiện tại", example = "84")
    private Integer maxPurchasableQuantity;

    @Schema(description = "Thời điểm thêm vào giỏ", example = "2026-10-07T10:00:00+07:00")
    private OffsetDateTime addedAt;
}

