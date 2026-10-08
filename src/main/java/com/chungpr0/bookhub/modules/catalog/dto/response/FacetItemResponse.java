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
@Schema(description = "Thông tin một mục lọc phân loại")
public class FacetItemResponse {

    @Schema(description = "Giá trị thuộc tính", example = "VI")
    private String value;

    @Schema(description = "Số lượng sách thỏa mãn", example = "45")
    private long count;
}

