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
@Schema(description = "Chi tiết thông tin tác giả dành cho Quản trị viên (AAU-02)")
public class AdminAuthorDetailResponse {

    @Schema(description = "ID tác giả", example = "7")
    private Long id;

    @Schema(description = "Tên tác giả", example = "Paulo Coelho")
    private String name;

    @Schema(description = "Đường dẫn thân thiện (slug)", example = "paulo-coelho")
    private String slug;

    @Schema(description = "Tiểu sử tóm tắt", example = "Paulo Coelho sinh năm 1947 tại Rio de Janeiro...")
    private String biography;

    @Schema(description = "URL ảnh chân dung", example = "https://cdn.bookhub.vn/authors/paulo-coelho.webp")
    private String avatarUrl;

    @Schema(description = "Số lượng tác phẩm trong hệ thống", example = "12")
    private long bookCount;

    @Schema(description = "Thời gian tạo", example = "2026-01-15T10:00:00+07:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Thời gian cập nhật", example = "2026-03-01T14:20:00+07:00")
    private OffsetDateTime updatedAt;
}

