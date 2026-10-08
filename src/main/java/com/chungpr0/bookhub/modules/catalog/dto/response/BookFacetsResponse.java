package com.chungpr0.bookhub.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Tập hợp các bộ lọc phân loại (Facets) cho danh mục sách")
public class BookFacetsResponse {

    @Schema(description = "Phân loại theo ngôn ngữ")
    @Builder.Default
    private List<FacetItemResponse> languages = new ArrayList<>();

    @Schema(description = "Phân loại theo hình thức bìa")
    @Builder.Default
    private List<FacetItemResponse> coverTypes = new ArrayList<>();

    @Schema(description = "Phân loại theo các khoảng giá bán")
    @Builder.Default
    private List<PriceRangeFacetResponse> priceRanges = new ArrayList<>();
}

