package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorDetailResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.AuthorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testAuthor = Author.builder()
                .id(1L)
                .name("Paulo Coelho")
                .slug("paulo-coelho")
                .biography("Nhà văn nổi tiếng thế giới")
                .avatarUrl("https://cdn.bookhub.vn/authors/paulo-coelho.webp")
                .build();
    }

    @Test
    @DisplayName("getAuthors - Thành công trả về danh sách phân trang")
    void testGetAuthors_Success() {
        when(authorRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testAuthor)));
        when(authorRepository.countActiveBooksByAuthorId(1L)).thenReturn(8);

        PageResponse<AuthorCardResponse> response = authorService.getAuthors(0, 20);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Paulo Coelho");
        assertThat(response.getItems().get(0).getBookCount()).isEqualTo(8);
        assertThat(response.getPage().getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAuthorBySlug - Thành công trả về chi tiết tác giả")
    void testGetAuthorBySlug_Success() {
        when(authorRepository.findBySlug("paulo-coelho")).thenReturn(Optional.of(testAuthor));
        when(authorRepository.countActiveBooksByAuthorId(1L)).thenReturn(8);
        when(bookRepository.findTopBooksByAuthorId(eq(1L), any())).thenReturn(Collections.emptyList());

        AuthorDetailResponse detail = authorService.getAuthorBySlug("paulo-coelho");

        assertThat(detail).isNotNull();
        assertThat(detail.getName()).isEqualTo("Paulo Coelho");
        assertThat(detail.getBookCount()).isEqualTo(8);
    }

    @Test
    @DisplayName("getAuthorBySlug - Ném lỗi AUTHOR_NOT_FOUND khi tác giả không tồn tại")
    void testGetAuthorBySlug_NotFound() {
        when(authorRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.getAuthorBySlug("unknown"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND);
                });
    }
}

