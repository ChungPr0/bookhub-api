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
public class OrderFirstItemResponse {

    @Schema(description = "ID cuốn sách", example = "101")
    private Long bookId;

    @Schema(description = "Tên cuốn sách đầu tiên", example = "Nhà Giả Kim")
    private String title;

    @Schema(description = "Ảnh bìa cuốn sách", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String thumbnailUrl;
}

