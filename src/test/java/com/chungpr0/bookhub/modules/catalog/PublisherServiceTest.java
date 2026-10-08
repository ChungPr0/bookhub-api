package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherDetailResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.PublisherServiceImpl;
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
class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private PublisherServiceImpl publisherService;

    private Publisher testPublisher;

    @BeforeEach
    void setUp() {
        testPublisher = Publisher.builder()
                .id(1L)
                .name("NXB Hội Nhà Văn")
                .slug("nxb-hoi-nha-van")
                .address("Hà Nội")
                .website("https://nxbhoinhavan.vn")
                .build();
    }

    @Test
    @DisplayName("getPublishers - Thành công trả về danh sách phân trang")
    void testGetPublishers_Success() {
        when(publisherRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testPublisher)));
        when(publisherRepository.countActiveBooksByPublisherId(1L)).thenReturn(15);

        PageResponse<PublisherCardResponse> response = publisherService.getPublishers(0, 20);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("NXB Hội Nhà Văn");
        assertThat(response.getItems().get(0).getBookCount()).isEqualTo(15);
    }

    @Test
    @DisplayName("getPublisherBySlug - Thành công trả về chi tiết NXB")
    void testGetPublisherBySlug_Success() {
        when(publisherRepository.findBySlug("nxb-hoi-nha-van")).thenReturn(Optional.of(testPublisher));
        when(publisherRepository.countActiveBooksByPublisherId(1L)).thenReturn(15);
        when(bookRepository.findTopBooksByPublisherId(eq(1L), any())).thenReturn(Collections.emptyList());

        PublisherDetailResponse detail = publisherService.getPublisherBySlug("nxb-hoi-nha-van");

        assertThat(detail).isNotNull();
        assertThat(detail.getName()).isEqualTo("NXB Hội Nhà Văn");
        assertThat(detail.getBookCount()).isEqualTo(15);
    }

    @Test
    @DisplayName("getPublisherBySlug - Ném lỗi PUBLISHER_NOT_FOUND khi slug không tồn tại")
    void testGetPublisherBySlug_NotFound() {
        when(publisherRepository.findBySlug("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.getPublisherBySlug("unknown"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    AppException appEx = (AppException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PUBLISHER_NOT_FOUND);
                });
    }
}

