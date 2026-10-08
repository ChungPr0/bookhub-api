package com.chungpr0.bookhub.modules.cart.dto.response;

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
@Schema(description = "Thông tin tóm tắt tác giả của sách")
public class CartItemAuthorResponse {

    @Schema(description = "ID tác giả", example = "7")
    private Long id;

    @Schema(description = "Tên tác giả", example = "Paulo Coelho")
    private String name;

    @Schema(description = "Slug tác giả", example = "paulo-coelho")
    private String slug;
}

