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
@Schema(description = "Chi tiết danh mục kèm breadcrumb và danh mục con")
public class CategoryDetailResponse {

    @Schema(description = "ID danh mục", example = "5")
    private Long id;

    @Schema(description = "Tên danh mục", example = "Tiểu thuyết")
    private String name;

    @Schema(description = "Slug danh mục", example = "tieu-thuyet")
    private String slug;

    @Schema(description = "Mô tả danh mục", example = "Các tác phẩm tiểu thuyết kinh điển và hiện đại chọn lọc")
    private String description;

    @Schema(description = "Số lượng sách mở bán", example = "85")
    private int bookCount;

    @Schema(description = "Chuỗi điều hướng breadcrumb từ gốc đến danh mục hiện tại")
    @Builder.Default
    private List<CategoryBreadcrumbItem> breadcrumb = new ArrayList<>();

    @Schema(description = "Danh sách danh mục con trực tiếp")
    @Builder.Default
    private List<CategoryChildResponse> children = new ArrayList<>();
}

