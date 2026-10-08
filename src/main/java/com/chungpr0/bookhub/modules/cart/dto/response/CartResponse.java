package com.chungpr0.bookhub.modules.cart.dto.response;

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
@Schema(description = "Thông tin toàn bộ giỏ hàng của khách hàng")
public class CartResponse {

    @Schema(description = "ID giỏ hàng", example = "15")
    private Long cartId;

    @Schema(description = "Danh sách chi tiết các món hàng trong giỏ")
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();

    @Schema(description = "Tổng kết thông tin tài chính của giỏ hàng")
    private CartSummaryResponse summary;

    @Schema(description = "Thời điểm cập nhật giỏ hàng gần nhất", example = "2026-10-07T10:00:00+07:00")
    private OffsetDateTime updatedAt;
}

