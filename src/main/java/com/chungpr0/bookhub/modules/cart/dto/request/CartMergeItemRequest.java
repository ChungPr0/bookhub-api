package com.chungpr0.bookhub.modules.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Mục sách cần gộp từ giỏ hàng vãng lai")
public class CartMergeItemRequest {

    @NotNull(message = "ID sách không được để trống")
    @Schema(description = "ID cuốn sách trong LocalStorage", example = "101")
    private Long bookId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải từ 1 đến 99")
    @Max(value = 99, message = "Số lượng phải từ 1 đến 99")
    @Schema(description = "Số lượng sách từ LocalStorage (1 - 99)", example = "2")
    private Integer quantity;
}

