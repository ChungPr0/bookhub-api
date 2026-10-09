package com.chungpr0.bookhub.modules.catalog.media.service.impl;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.SlugUtils;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaBatchItemResponse;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaBatchUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.enums.MediaFolder;
import com.chungpr0.bookhub.modules.catalog.media.service.MediaService;
import com.chungpr0.bookhub.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5 MB

    @Override
    public MediaUploadResponse uploadSingle(MultipartFile file, MediaFolder folder, UserPrincipal principal) {
        validateFolderAccess(folder, principal);
        validateFile(file);

        String generatedBase = generateWebpFileName(file.getOriginalFilename());
        OffsetDateTime now = OffsetDateTime.now();
        String path = String.format("%s/%d/%02d/%s", folder.name().toLowerCase(), now.getYear(), now.getMonthValue(), generatedBase);

        String url = "https://cdn.bookhub.vn/" + path;
        String thumbUrl = "https://cdn.bookhub.vn/" + path.replace(".webp", "-thumb.webp");

        return MediaUploadResponse.builder()
                .url(url)
                .thumbnailUrl(thumbUrl)
                .fileName(generatedBase)
                .fileSizeBytes(file.getSize())
                .width(1200)
                .height(1800)
                .mimeType("image/webp")
                .build();
    }

    @Override
    public MediaBatchUploadResponse uploadBatch(List<MultipartFile> files, MediaFolder folder, UserPrincipal principal) {
        validateFolderAccess(folder, principal);

        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.FILE_REQUIRED, "Vui lòng chọn ít nhất một tệp tin hình ảnh");
        }

        if (files.size() > 10) {
            throw new AppException(ErrorCode.TOO_MANY_FILES, "Số lượng ảnh tải lên cùng lúc vượt quá giới hạn tối đa 10 ảnh");
        }

        // Atomic validation: all files must pass validation first
        for (MultipartFile file : files) {
            validateFile(file);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<MediaBatchItemResponse> items = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String generatedBase = generateWebpFileName(file.getOriginalFilename());
            String path = String.format("%s/%d/%02d/%s", folder.name().toLowerCase(), now.getYear(), now.getMonthValue(), generatedBase);

            String url = "https://cdn.bookhub.vn/" + path;
            String thumbUrl = "https://cdn.bookhub.vn/" + path.replace(".webp", "-thumb.webp");

            items.add(MediaBatchItemResponse.builder()
                    .sortOrder(i)
                    .url(url)
                    .thumbnailUrl(thumbUrl)
                    .fileName(generatedBase)
                    .fileSizeBytes(file.getSize())
                    .width(1000)
                    .height(1400)
                    .build());
        }

        return MediaBatchUploadResponse.builder()
                .totalUploaded(items.size())
                .items(items)
                .build();
    }

    private void validateFolderAccess(MediaFolder folder, UserPrincipal principal) {
        if (principal == null) {
            return;
        }
        boolean isCustomer = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));

        if (isCustomer && folder != MediaFolder.AVATARS) {
            throw new AppException(ErrorCode.ACCESS_DENIED, "Bạn không có quyền tải ảnh vào thư mục này");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_REQUIRED, "Vui lòng chọn tệp tin hình ảnh cần tải lên");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE, "Dung lượng ảnh vượt quá giới hạn tối đa 5MB");
        }

        try {
            byte[] bytes = file.getBytes();
            if (!isValidImageMagicBytes(bytes)) {
                throw new AppException(ErrorCode.UNSUPPORTED_FILE_TYPE, "Hệ thống chỉ hỗ trợ định dạng JPG, PNG hoặc WEBP");
            }
        } catch (IOException e) {
            throw new AppException(ErrorCode.STORAGE_SERVICE_ERROR, "Không thể đọc dữ liệu tệp tin hình ảnh");
        }
    }

    private boolean isValidImageMagicBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }

        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47
        if ((bytes[0] & 0xFF) == 0x89 && (bytes[1] & 0xFF) == 0x50 && (bytes[2] & 0xFF) == 0x4E && (bytes[3] & 0xFF) == 0x47) {
            return true;
        }

        // WEBP: RIFF (bytes 0..3) and WEBP (bytes 8..11)
        return bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private String generateWebpFileName(String originalFilename) {
        String baseName = "image-" + UUID.randomUUID().toString().substring(0, 8);
        if (originalFilename != null && !originalFilename.isBlank()) {
            int dotIndex = originalFilename.lastIndexOf('.');
            String nameWithoutExt = dotIndex > 0 ? originalFilename.substring(0, dotIndex) : originalFilename;
            String slug = SlugUtils.toSlug(nameWithoutExt);
            if (!slug.isBlank()) {
                baseName = slug;
            }
        }
        return baseName + ".webp";
    }
}

