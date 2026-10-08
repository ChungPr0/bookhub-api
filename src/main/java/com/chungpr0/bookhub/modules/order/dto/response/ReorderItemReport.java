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
public class ReorderItemReport {

    @Schema(description = "ID cuốn sách", example = "101")
    private Long bookId;

    @Schema(description = "Tên cuốn sách", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Số lượng", example = "2")
    private Integer quantity;

    @Schema(description = "Lý do hoặc ghi chú (nếu bị bỏ qua hoặc điều chỉnh)", example = "Đã thêm vào giỏ")
    private String reason;
}

