package com.chungpr0.bookhub.modules.catalog.dto.response;

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
@Schema(description = "Hình ảnh minh họa sách")
public class BookImageResponse {

    @Schema(description = "ID hình ảnh", example = "201")
    private Long id;

    @Schema(description = "URL hình ảnh", example = "https://cdn.bookhub.vn/books/101/cover.webp")
    private String url;

    @Schema(description = "Thứ tự sắp xếp (0 là ảnh bìa chính)", example = "0")
    private int sortOrder;
}

