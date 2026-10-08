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
@Schema(description = "Dữ liệu một node trong cây danh mục đa cấp")
public class CategoryTreeResponse {

    @Schema(description = "ID danh mục", example = "1")
    private Long id;

    @Schema(description = "Tên danh mục", example = "Văn học")
    private String name;

    @Schema(description = "Đường dẫn thân thiện (slug)", example = "van-hoc")
    private String slug;

    @Schema(description = "Số lượng sách đang mở bán", example = "142")
    private int bookCount;

    @Schema(description = "Danh sách danh mục con trực thuộc")
    @Builder.Default
    private List<CategoryTreeResponse> children = new ArrayList<>();
}

