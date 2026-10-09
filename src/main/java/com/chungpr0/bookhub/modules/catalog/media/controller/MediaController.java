package com.chungpr0.bookhub.modules.catalog.media.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaBatchUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.enums.MediaFolder;
import com.chungpr0.bookhub.modules.catalog.media.service.MediaService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media/upload")
@RequiredArgsConstructor
@Tag(name = "6.6 Quản lý Tệp Media & Tải lên (Media Pipeline)", description = "APIs pipeline tiếp nhận tệp tin nhị phân hình ảnh đơn lẻ và upload hàng loạt kéo thả")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "MED-01: Tải lên một ảnh đơn lẻ",
            description = "Tải lên 1 ảnh đại diện (CUSTOMER chỉ được upload folder AVATARS) hoặc ảnh sách/tác giả/banner (STAFF/ADMIN).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<MediaUploadResponse>> uploadSingle(
            @Parameter(description = "Tệp tin hình ảnh (JPEG, PNG, WEBP <= 5MB)", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Thư mục phân loại lưu trữ (BOOKS, AUTHORS, AVATARS, BANNERS)", example = "BOOKS", required = true)
            @RequestParam("folder") MediaFolder folder,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MediaUploadResponse response = mediaService.uploadSingle(file, folder, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tải lên hình ảnh thành công", response));
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "MED-02: Tải lên hàng loạt ảnh kéo thả",
            description = "Tải lên mảng từ 1 đến 10 ảnh kéo thả cùng lúc trên giao diện quản trị (xử lý atomic - kiểm tra toàn bộ tệp tin).",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<MediaBatchUploadResponse>> uploadBatch(
            @Parameter(description = "Danh sách tệp tin ảnh (tối đa 10 ảnh, mỗi ảnh <= 5MB)", required = true)
            @RequestPart("files") List<MultipartFile> files,
            @Parameter(description = "Thư mục phân loại lưu trữ (BOOKS, AUTHORS, BANNERS)", example = "BOOKS", required = true)
            @RequestParam("folder") MediaFolder folder,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MediaBatchUploadResponse response = mediaService.uploadBatch(files, folder, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tải lên hàng loạt hình ảnh thành công", response));
    }
}

