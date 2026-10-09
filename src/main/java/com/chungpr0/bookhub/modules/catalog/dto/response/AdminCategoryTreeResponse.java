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
@Schema(description = "Node trong cây danh mục đa cấp phục vụ Quản trị viên (ACT-01)")
public class AdminCategoryTreeResponse {

    @Schema(description = "ID danh mục", example = "1")
    private Long id;

    @Schema(description = "Tên danh mục", example = "Văn học")
    private String name;

    @Schema(description = "Đường dẫn thân thiện (slug)", example = "van-hoc")
    private String slug;

    @Schema(description = "Mô tả danh mục", example = "Tác phẩm văn học trong và ngoài nước")
    private String description;

    @Schema(description = "ID danh mục cha", example = "null")
    private Long parentId;

    @Schema(description = "Thứ tự sắp xếp", example = "1")
    private int sortOrder;

    @Schema(description = "Cấp bậc danh mục (1, 2 hoặc 3)", example = "1")
    private int depth;

    @Schema(description = "Số lượng sách trực tiếp thuộc danh mục này", example = "15")
    private long bookCount;

    @Schema(description = "Tổng số lượng sách bao gồm cả danh mục con", example = "142")
    private long totalBookCount;

    @Schema(description = "Danh sách danh mục con trực thuộc")
    @Builder.Default
    private List<AdminCategoryTreeResponse> children = new ArrayList<>();
}

