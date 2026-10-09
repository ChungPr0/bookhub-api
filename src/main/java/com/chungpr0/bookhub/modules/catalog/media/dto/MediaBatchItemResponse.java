package com.chungpr0.bookhub.modules.catalog.media.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin một hình ảnh trong mảng upload hàng loạt")
public class MediaBatchItemResponse {

    @Schema(description = "Thứ tự sắp xếp của ảnh kéo thả", example = "0")
    private int sortOrder;

    @Schema(description = "Đường dẫn URL ảnh chất lượng tiêu chuẩn", example = "https://cdn.bookhub.vn/books/2026/10/cay-cam-goc-nghieng.webp")
    private String url;

    @Schema(description = "Đường dẫn URL ảnh thu nhỏ", example = "https://cdn.bookhub.vn/books/2026/10/cay-cam-goc-nghieng-thumb.webp")
    private String thumbnailUrl;

    @Schema(description = "Tên tệp tin sau chuẩn hóa", example = "cay-cam-goc-nghieng.webp")
    private String fileName;

    @Schema(description = "Dung lượng tệp tin (bytes)", example = "382400")
    private long fileSizeBytes;

    @Schema(description = "Chiều rộng hình ảnh (pixels)", example = "1000")
    private int width;

    @Schema(description = "Chiều cao hình ảnh (pixels)", example = "1400")
    private int height;
}

