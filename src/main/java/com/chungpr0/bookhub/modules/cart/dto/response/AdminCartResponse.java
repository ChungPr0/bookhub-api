package com.chungpr0.bookhub.modules.cart.dto.response;

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
@Schema(description = "Thông tin tóm tắt giỏ hàng phía quản trị viên")
public class AdminCartResponse {

    @Schema(description = "ID giỏ hàng", example = "15")
    private Long cartId;

    @Schema(description = "ID khách hàng sở hữu", example = "1")
    private Long customerId;

    @Schema(description = "Tên khách hàng", example = "Nguyễn Văn A")
    private String customerName;

    @Schema(description = "Số điện thoại khách hàng", example = "0988888888")
    private String customerPhone;

    @Schema(description = "Số lượng đầu sách trong giỏ", example = "2")
    private Integer itemCount;

    @Schema(description = "Tổng số lượng cuốn sách trong giỏ", example = "7")
    private Integer totalQuantity;

    @Schema(description = "Tổng giá trị giỏ hàng (VND)", example = "558400")
    private Long subtotal;

    @Schema(description = "Thời điểm cập nhật giỏ gần nhất", example = "2026-10-07T10:00:00+07:00")
    private OffsetDateTime updatedAt;
}

