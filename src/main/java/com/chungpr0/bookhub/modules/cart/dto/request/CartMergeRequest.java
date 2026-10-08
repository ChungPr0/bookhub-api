package com.chungpr0.bookhub.modules.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload gộp giỏ hàng vãng lai sau khi đăng nhập")
public class CartMergeRequest {

    @NotNull(message = "Danh sách sản phẩm không được null")
    @Valid
    @Schema(description = "Danh sách các đầu sách cần gộp từ giỏ hàng vãng lai")
    @Builder.Default
    private List<CartMergeItemRequest> items = new ArrayList<>();
}

