package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.AdminAuthorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AdminAuthorServiceImpl adminAuthorService;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testAuthor = Author.builder()
                .id(1L)
                .name("Nguyễn Nhật Ánh")
                .slug("nguyen-nhat-anh")
                .biography("Nhà văn nổi tiếng của Việt Nam")
                .avatarUrl("https://bookhub.com/media/authors/nguyen-nhat-anh.jpg")
                .build();
    }

    @Test
    @DisplayName("getAdminAuthors - Thành công phân trang và tìm kiếm tác giả")
    void testGetAdminAuthors_Success() {
        when(authorRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Author>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testAuthor)));
        when(authorRepository.countTotalBooksByAuthorId(1L)).thenReturn(25L);

        PageResponse<AdminAuthorResponse> response = adminAuthorService.getAdminAuthors("Nguyễn", 0, 10, "name,asc");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Nguyễn Nhật Ánh");
        assertThat(response.getItems().get(0).getBookCount()).isEqualTo(25L);
    }

    @Test
    @DisplayName("getAdminAuthorDetail - Thành công lấy thông tin chi tiết tác giả")
    void testGetAdminAuthorDetail_Success() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.countTotalBooksByAuthorId(1L)).thenReturn(25L);

        AdminAuthorDetailResponse detail = adminAuthorService.getAdminAuthorDetail(1L);

        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(1L);
        assertThat(detail.getName()).isEqualTo("Nguyễn Nhật Ánh");
        assertThat(detail.getBookCount()).isEqualTo(25L);
    }

    @Test
    @DisplayName("getAdminAuthorDetail - Ném lỗi AUTHOR_NOT_FOUND khi ID không tồn tại")
    void testGetAdminAuthorDetail_NotFound() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminAuthorService.getAdminAuthorDetail(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND));
    }

    @Test
    @DisplayName("createAuthor - Thành công tạo mới tác giả và sinh slug duy nhất")
    void testCreateAuthor_Success() {
        CreateAuthorRequest request = CreateAuthorRequest.builder()
                .name("Haruki Murakami")
                .biography("Nhà văn Nhật Bản")
                .avatarUrl("https://bookhub.com/media/authors/haruki.jpg")
                .build();

        when(authorRepository.existsBySlug("haruki-murakami")).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(5L);
            return author;
        });

        AdminAuthorResponse response = adminAuthorService.createAuthor(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("Haruki Murakami");
        assertThat(response.getSlug()).isEqualTo("haruki-murakami");
    }

    @Test
    @DisplayName("createAuthor - Sinh slug hậu tố số khi slug cơ bản đã tồn tại")
    void testCreateAuthor_DisambiguateSlug() {
        CreateAuthorRequest request = CreateAuthorRequest.builder()
                .name("Nguyễn Nhật Ánh")
                .biography("Tác giả trùng tên")
                .build();

        when(authorRepository.existsBySlug("nguyen-nhat-anh")).thenReturn(true);
        when(authorRepository.existsBySlug("nguyen-nhat-anh-2")).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(6L);
            return author;
        });

        AdminAuthorResponse response = adminAuthorService.createAuthor(request);

        assertThat(response).isNotNull();
        assertThat(response.getSlug()).isEqualTo("nguyen-nhat-anh-2");
    }

    @Test
    @DisplayName("updateAuthor - Thành công cập nhật tác giả")
    void testUpdateAuthor_Success() {
        UpdateAuthorRequest request = UpdateAuthorRequest.builder()
                .name("Nguyễn Nhật Ánh (Updated)")
                .biography("Cập nhật tiểu sử")
                .build();

        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.existsBySlugAndIdNot("nguyen-nhat-anh-updated", 1L)).thenReturn(false);
        when(authorRepository.save(any(Author.class))).thenReturn(testAuthor);
        when(authorRepository.countTotalBooksByAuthorId(1L)).thenReturn(25L);

        AdminAuthorResponse response = adminAuthorService.updateAuthor(1L, request);

        assertThat(response).isNotNull();
        assertThat(testAuthor.getName()).isEqualTo("Nguyễn Nhật Ánh (Updated)");
        assertThat(testAuthor.getBiography()).isEqualTo("Cập nhật tiểu sử");
    }

    @Test
    @DisplayName("deleteAuthor - Ném lỗi AUTHOR_HAS_BOOKS khi tác giả còn sách")
    void testDeleteAuthor_HasBooks() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.countTotalBooksByAuthorId(1L)).thenReturn(10L);

        assertThatThrownBy(() -> adminAuthorService.deleteAuthor(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.AUTHOR_HAS_BOOKS));
    }

    @Test
    @DisplayName("deleteAuthor - Thành công xóa tác giả khi không có sách")
    void testDeleteAuthor_Success() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(authorRepository.countTotalBooksByAuthorId(1L)).thenReturn(0L);

        adminAuthorService.deleteAuthor(1L);

        verify(authorRepository).delete(testAuthor);
    }
}

