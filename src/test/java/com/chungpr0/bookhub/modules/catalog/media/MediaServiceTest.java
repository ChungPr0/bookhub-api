package com.chungpr0.bookhub.modules.catalog.media;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaBatchUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.dto.MediaUploadResponse;
import com.chungpr0.bookhub.modules.catalog.media.enums.MediaFolder;
import com.chungpr0.bookhub.modules.catalog.media.service.impl.MediaServiceImpl;
import com.chungpr0.bookhub.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MediaServiceTest {

    private MediaServiceImpl mediaService;
    private UserPrincipal adminPrincipal;
    private UserPrincipal customerPrincipal;

    private static final byte[] VALID_JPEG = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] VALID_PNG = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] VALID_WEBP = new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
    private static final byte[] INVALID_BYTES = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    @BeforeEach
    void setUp() {
        mediaService = new MediaServiceImpl();
        adminPrincipal = UserPrincipal.create(1L, "0922222222", Role.ADMIN, AccountStatus.ACTIVE, 0);
        customerPrincipal = UserPrincipal.create(2L, "0911111111", Role.CUSTOMER, AccountStatus.ACTIVE, 0);
    }

    @Test
    @DisplayName("uploadSingle - Thành công tải lên file JPEG hợp lệ bởi Admin")
    void testUploadSingle_JpegSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "book-cover.jpg", "image/jpeg", VALID_JPEG);

        MediaUploadResponse response = mediaService.uploadSingle(file, MediaFolder.BOOKS, adminPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getUrl()).contains("books/");
        assertThat(response.getUrl()).endsWith(".webp");
        assertThat(response.getThumbnailUrl()).contains("-thumb.webp");
        assertThat(response.getMimeType()).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("uploadSingle - Thành công tải lên avatar bởi Customer")
    void testUploadSingle_CustomerAvatarSuccess() {
        MockMultipartFile file = new MockMultipartFile("file", "my-avatar.png", "image/png", VALID_PNG);

        MediaUploadResponse response = mediaService.uploadSingle(file, MediaFolder.AVATARS, customerPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getUrl()).contains("avatars/");
    }

    @Test
    @DisplayName("uploadSingle - Ném lỗi ACCESS_DENIED khi Customer cố tình tải ảnh vào thư mục ngoài AVATARS")
    void testUploadSingle_CustomerForbiddenFolder() {
        MockMultipartFile file = new MockMultipartFile("file", "banner.png", "image/png", VALID_PNG);

        assertThatThrownBy(() -> mediaService.uploadSingle(file, MediaFolder.BANNERS, customerPrincipal))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    @DisplayName("uploadSingle - Ném lỗi UNSUPPORTED_FILE_TYPE khi magic bytes không hợp lệ")
    void testUploadSingle_InvalidMagicBytes() {
        MockMultipartFile file = new MockMultipartFile("file", "hack.exe", "image/jpeg", INVALID_BYTES);

        assertThatThrownBy(() -> mediaService.uploadSingle(file, MediaFolder.BOOKS, adminPrincipal))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_FILE_TYPE));
    }

    @Test
    @DisplayName("uploadSingle - Ném lỗi FILE_REQUIRED khi file rỗng")
    void testUploadSingle_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> mediaService.uploadSingle(file, MediaFolder.BOOKS, adminPrincipal))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FILE_REQUIRED));
    }

    @Test
    @DisplayName("uploadSingle - Ném lỗi FILE_TOO_LARGE khi file vượt quá 5MB")
    void testUploadSingle_TooLarge() {
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile("file", "huge.jpg", "image/jpeg", largeBytes);

        assertThatThrownBy(() -> mediaService.uploadSingle(file, MediaFolder.BOOKS, adminPrincipal))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.FILE_TOO_LARGE));
    }

    @Test
    @DisplayName("uploadBatch - Thành công tải lên nhiều ảnh đồng thời")
    void testUploadBatch_Success() {
        MockMultipartFile file1 = new MockMultipartFile("files", "img1.png", "image/png", VALID_PNG);
        MockMultipartFile file2 = new MockMultipartFile("files", "img2.webp", "image/webp", VALID_WEBP);

        MediaBatchUploadResponse response = mediaService.uploadBatch(List.of(file1, file2), MediaFolder.BOOKS, adminPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.getTotalUploaded()).isEqualTo(2);
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getSortOrder()).isEqualTo(0);
        assertThat(response.getItems().get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("uploadBatch - Ném lỗi TOO_MANY_FILES khi danh sách quá 10 ảnh")
    void testUploadBatch_TooManyFiles() {
        List<org.springframework.web.multipart.MultipartFile> list = java.util.Collections.nCopies(
                11,
                new MockMultipartFile("files", "img.png", "image/png", VALID_PNG)
        );

        assertThatThrownBy(() -> mediaService.uploadBatch(list, MediaFolder.BOOKS, adminPrincipal))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.TOO_MANY_FILES));
    }

    @Test
    @DisplayName("uploadBatch - Nguyên tử: một file lỗi khiến toàn bộ batch bị từ chối")
    void testUploadBatch_AtomicFailure() {
        MockMultipartFile validFile = new MockMultipartFile("files", "valid.png", "image/png", VALID_PNG);
        MockMultipartFile corruptFile = new MockMultipartFile("files", "corrupt.jpg", "image/jpeg", INVALID_BYTES);

        assertThatThrownBy(() -> mediaService.uploadBatch(List.of(validFile, corruptFile), MediaFolder.BOOKS, adminPrincipal))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.UNSUPPORTED_FILE_TYPE));
    }
}

