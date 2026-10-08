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
@Schema(description = "Thông tin tóm tắt danh mục")
public class CategorySummaryResponse {

    @Schema(description = "ID danh mục", example = "5")
    private Long id;

    @Schema(description = "Tên danh mục", example = "Tiểu thuyết")
    private String name;

    @Schema(description = "Slug danh mục", example = "tieu-thuyet")
    private String slug;
}

