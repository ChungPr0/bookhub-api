package com.chungpr0.bookhub.modules.catalog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết một danh mục sách dành cho Quản trị viên")
public class AdminCategoryResponse {

    @Schema(description = "ID danh mục", example = "8")
    private Long id;

    @Schema(description = "Tên danh mục", example = "Kinh tế - Khởi nghiệp")
    private String name;

    @Schema(description = "Đường dẫn thân thiện (slug)", example = "kinh-te-khoi-nghiep")
    private String slug;

    @Schema(description = "Mô tả danh mục", example = "Sách kỹ năng kinh doanh, quản trị và tài chính")
    private String description;

    @Schema(description = "ID danh mục cha", example = "1")
    private Long parentId;

    @Schema(description = "Thứ tự sắp xếp", example = "2")
    private int sortOrder;

    @Schema(description = "Thời gian tạo", example = "2026-10-07T17:45:56+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-10-07T17:45:56+07:00")
    private OffsetDateTime updatedAt;
}

