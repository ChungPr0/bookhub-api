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
@Schema(description = "Chi tiết tác giả và các tác phẩm tiêu biểu")
public class AuthorDetailResponse {

    @Schema(description = "ID tác giả", example = "7")
    private Long id;

    @Schema(description = "Tên tác giả", example = "Paulo Coelho")
    private String name;

    @Schema(description = "Slug tác giả", example = "paulo-coelho")
    private String slug;

    @Schema(description = "Tiểu sử tác giả", example = "Paulo Coelho là một trong những nhà văn có tác phẩm được dịch ra nhiều thứ tiếng nhất...")
    private String biography;

    @Schema(description = "URL ảnh chân dung", example = "https://cdn.bookhub.vn/authors/paulo-coelho.webp")
    private String avatarUrl;

    @Schema(description = "Số lượng tác phẩm", example = "12")
    private int bookCount;

    @Schema(description = "Danh sách tối đa 8 tác phẩm tiêu biểu")
    @Builder.Default
    private List<BookCardResponse> topBooks = new ArrayList<>();
}

