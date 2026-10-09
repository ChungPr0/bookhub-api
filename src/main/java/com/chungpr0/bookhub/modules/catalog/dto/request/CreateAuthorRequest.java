package com.chungpr0.bookhub.modules.catalog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu thêm mới tác giả")
public class CreateAuthorRequest {

    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(min = 2, max = 150, message = "Tên tác giả phải từ 2 đến 150 ký tự")
    @Schema(description = "Tên đầy đủ của tác giả", example = "Nguyễn Nhật Ánh")
    private String name;

    @Size(max = 5000, message = "Tiểu sử tác giả tối đa 5000 ký tự")
    @Schema(description = "Tiểu sử tóm tắt của tác giả", example = "Nguyễn Nhật Ánh là nhà văn viết cho thanh thiếu niên nổi tiếng tại Việt Nam...")
    private String biography;

    @Size(max = 500, message = "Đường dẫn ảnh đại diện tối đa 500 ký tự")
    @Schema(description = "URL ảnh chân dung tác giả", example = "https://cdn.bookhub.vn/authors/nguyen-nhat-anh.webp")
    private String avatarUrl;
}

