package com.chungpr0.bookhub.modules.catalog.media.dto;

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
@Schema(description = "Kết quả tải lên hàng loạt ảnh kéo thả (MED-02)")
public class MediaBatchUploadResponse {

    @Schema(description = "Tổng số lượng hình ảnh đã tải lên thành công", example = "3")
    private int totalUploaded;

    @Schema(description = "Danh sách chi tiết các hình ảnh đã tải lên theo thứ tự")
    @Builder.Default
    private List<MediaBatchItemResponse> items = new ArrayList<>();
}

