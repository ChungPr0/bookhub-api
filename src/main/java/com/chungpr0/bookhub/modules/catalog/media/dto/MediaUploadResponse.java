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
@Schema(description = "Kết quả tải lên một tệp tin hình ảnh đơn lẻ (MED-01)")
public class MediaUploadResponse {

    @Schema(description = "Đường dẫn URL ảnh chất lượng tiêu chuẩn trên CDN", example = "https://cdn.bookhub.vn/books/2026/10/nha-gia-kim-cover.webp")
    private String url;

    @Schema(description = "Đường dẫn URL ảnh thu nhỏ (Thumbnail)", example = "https://cdn.bookhub.vn/books/2026/10/nha-gia-kim-cover-thumb.webp")
    private String thumbnailUrl;

    @Schema(description = "Tên tệp tin sau chuẩn hóa", example = "nha-gia-kim-cover.webp")
    private String fileName;

    @Schema(description = "Dung lượng tệp tin (bytes)", example = "458200")
    private long fileSizeBytes;

    @Schema(description = "Chiều rộng hình ảnh (pixels)", example = "1200")
    private int width;

    @Schema(description = "Chiều cao hình ảnh (pixels)", example = "1800")
    private int height;

    @Schema(description = "MIME Type chuẩn", example = "image/webp")
    private String mimeType;
}

