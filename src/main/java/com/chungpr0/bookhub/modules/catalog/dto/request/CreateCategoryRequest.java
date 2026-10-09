package com.chungpr0.bookhub.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Yêu cầu tạo mới danh mục sách")
public class CreateCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    @Schema(description = "Tên danh mục sách", example = "Kinh tế - Khởi nghiệp")
    private String name;

    @Schema(description = "ID danh mục cha (null nếu là danh mục gốc)", example = "1")
    private Long parentId;

    @Size(max = 500, message = "Mô tả danh mục tối đa 500 ký tự")
    @Schema(description = "Mô tả tóm tắt về danh mục", example = "Sách kỹ năng kinh doanh, quản trị và tài chính")
    private String description;

    @Min(value = 0, message = "Thứ tự sắp xếp phải lớn hơn hoặc bằng 0")
    @Builder.Default
    @Schema(description = "Thứ tự sắp xếp hiển thị", example = "2")
    private Integer sortOrder = 0;
}

