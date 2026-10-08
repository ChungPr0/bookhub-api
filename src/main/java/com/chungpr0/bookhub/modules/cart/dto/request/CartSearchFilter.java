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
@Schema(description = "Tiêu chí tìm kiếm và quản lý giỏ hàng phía Admin")
public class CartSearchFilter {

    @Schema(description = "Từ khóa tìm kiếm theo tên khách hàng hoặc số điện thoại", example = "0988888888")
    private String keyword;

    @Schema(description = "Lọc giỏ hàng có sản phẩm (true) hoặc rỗng (false)", example = "true")
    private Boolean hasItems;
}

