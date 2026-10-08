package com.chungpr0.bookhub.modules.cart.dto.request;

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
@Schema(description = "Tiêu chí tìm kiếm và lọc danh sách yêu thích")
public class WishlistFilter {

    @Schema(description = "Từ khóa tìm kiếm theo tên cuốn sách", example = "Nhà giả kim")
    private String keyword;

    @Schema(description = "Lọc theo danh mục sách", example = "1")
    private Long categoryId;

    @Schema(description = "Chỉ lọc các cuốn sách còn hàng trong kho", example = "false")
    private Boolean inStockOnly;
}

