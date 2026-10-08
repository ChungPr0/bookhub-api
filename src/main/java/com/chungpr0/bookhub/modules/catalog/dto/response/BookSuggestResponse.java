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
@Schema(description = "Kết quả gợi ý autocomplete tìm kiếm nhanh")
public class BookSuggestResponse {

    @Schema(description = "Danh sách tối đa 5 đầu sách khớp nhất")
    @Builder.Default
    private List<BookSuggestItem> books = new ArrayList<>();

    @Schema(description = "Danh sách tác giả gợi ý")
    @Builder.Default
    private List<AuthorSummaryResponse> authors = new ArrayList<>();

    @Schema(description = "Danh sách danh mục gợi ý")
    @Builder.Default
    private List<CategorySummaryResponse> categories = new ArrayList<>();

    @Schema(description = "Danh sách từ khóa tìm kiếm phổ biến")
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
}

