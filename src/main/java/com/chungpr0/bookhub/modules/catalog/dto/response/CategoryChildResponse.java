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
@Schema(description = "Thông tin danh mục con trực tiếp")
public class CategoryChildResponse {

    @Schema(description = "ID danh mục con", example = "12")
    private Long id;

    @Schema(description = "Tên danh mục con", example = "Tiểu thuyết trinh thám")
    private String name;

    @Schema(description = "Slug danh mục con", example = "tieu-thuyet-trinh-tham")
    private String slug;

    @Schema(description = "Số lượng sách đang mở bán", example = "45")
    private int bookCount;
}

