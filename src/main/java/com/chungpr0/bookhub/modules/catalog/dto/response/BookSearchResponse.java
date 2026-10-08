package com.chungpr0.bookhub.modules.catalog.dto.response;

import com.chungpr0.bookhub.common.dto.PageMeta;
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
@Schema(description = "Kết quả tìm kiếm và lọc sách đa tiêu chí kèm phân trang và facets")
public class BookSearchResponse {

    @Schema(description = "Danh sách thẻ sách tìm thấy")
    @Builder.Default
    private List<BookCardResponse> items = new ArrayList<>();

    @Schema(description = "Thông tin phân trang")
    private PageMeta page;

    @Schema(description = "Dữ liệu bộ lọc phân loại tương ứng")
    private BookFacetsResponse facets;
}

