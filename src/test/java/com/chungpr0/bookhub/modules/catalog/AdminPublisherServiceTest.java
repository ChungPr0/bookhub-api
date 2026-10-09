package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.AdminPublisherServiceImpl;
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
class AdminPublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @InjectMocks
    private AdminPublisherServiceImpl adminPublisherService;

    private Publisher testPublisher;

    @BeforeEach
    void setUp() {
        testPublisher = Publisher.builder()
                .id(1L)
                .name("NXB Trẻ")
                .slug("nxb-tre")
                .address("161B Lý Chính Thắng, P. Võ Thị Sáu, Q.3, TP.HCM")
                .website("https://www.nxbtre.com.vn")
                .build();
    }

    @Test
    @DisplayName("getAdminPublishers - Thành công phân trang và tìm kiếm nhà xuất bản")
    void testGetAdminPublishers_Success() {
        when(publisherRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Publisher>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testPublisher)));
        when(publisherRepository.countTotalBooksByPublisherId(1L)).thenReturn(40L);

        PageResponse<AdminPublisherResponse> response = adminPublisherService.getAdminPublishers("Trẻ", 0, 10, "name,asc");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("NXB Trẻ");
        assertThat(response.getItems().get(0).getBookCount()).isEqualTo(40L);
    }

    @Test
    @DisplayName("getAdminPublisherDetail - Thành công lấy chi tiết nhà xuất bản")
    void testGetAdminPublisherDetail_Success() {
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(publisherRepository.countTotalBooksByPublisherId(1L)).thenReturn(40L);

        AdminPublisherDetailResponse detail = adminPublisherService.getAdminPublisherDetail(1L);

        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(1L);
        assertThat(detail.getName()).isEqualTo("NXB Trẻ");
        assertThat(detail.getBookCount()).isEqualTo(40L);
    }

    @Test
    @DisplayName("getAdminPublisherDetail - Ném lỗi PUBLISHER_NOT_FOUND khi ID không tồn tại")
    void testGetAdminPublisherDetail_NotFound() {
        when(publisherRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminPublisherService.getAdminPublisherDetail(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PUBLISHER_NOT_FOUND));
    }

    @Test
    @DisplayName("createPublisher - Thành công tạo mới nhà xuất bản")
    void testCreatePublisher_Success() {
        CreatePublisherRequest request = CreatePublisherRequest.builder()
                .name("NXB Kim Đồng")
                .address("55 Quang Trung, Hai Bà Trưng, Hà Nội")
                .website("https://nxbkimdong.com.vn")
                .build();

        when(publisherRepository.existsByName("NXB Kim Đồng")).thenReturn(false);
        when(publisherRepository.existsBySlug("nxb-kim-dong")).thenReturn(false);
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> {
            Publisher pub = invocation.getArgument(0);
            pub.setId(2L);
            return pub;
        });

        AdminPublisherResponse response = adminPublisherService.createPublisher(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("NXB Kim Đồng");
        assertThat(response.getSlug()).isEqualTo("nxb-kim-dong");
    }

    @Test
    @DisplayName("createPublisher - Ném lỗi PUBLISHER_NAME_DUPLICATE khi tên đã tồn tại")
    void testCreatePublisher_DuplicateName() {
        CreatePublisherRequest request = CreatePublisherRequest.builder()
                .name("NXB Trẻ")
                .build();

        when(publisherRepository.existsByName("NXB Trẻ")).thenReturn(true);

        assertThatThrownBy(() -> adminPublisherService.createPublisher(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PUBLISHER_NAME_DUPLICATE));
    }

    @Test
    @DisplayName("updatePublisher - Thành công cập nhật nhà xuất bản")
    void testUpdatePublisher_Success() {
        UpdatePublisherRequest request = UpdatePublisherRequest.builder()
                .name("NXB Trẻ (Updated)")
                .address("Địa chỉ mới")
                .website("https://nxbtre.vn")
                .build();

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(publisherRepository.existsByNameAndIdNot("NXB Trẻ (Updated)", 1L)).thenReturn(false);
        when(publisherRepository.existsBySlugAndIdNot("nxb-tre-updated", 1L)).thenReturn(false);
        when(publisherRepository.save(any(Publisher.class))).thenReturn(testPublisher);
        when(publisherRepository.countTotalBooksByPublisherId(1L)).thenReturn(40L);

        AdminPublisherResponse response = adminPublisherService.updatePublisher(1L, request);

        assertThat(response).isNotNull();
        assertThat(testPublisher.getName()).isEqualTo("NXB Trẻ (Updated)");
        assertThat(testPublisher.getAddress()).isEqualTo("Địa chỉ mới");
    }

    @Test
    @DisplayName("deletePublisher - Ném lỗi PUBLISHER_HAS_BOOKS khi nhà xuất bản còn sách")
    void testDeletePublisher_HasBooks() {
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(publisherRepository.countTotalBooksByPublisherId(1L)).thenReturn(15L);

        assertThatThrownBy(() -> adminPublisherService.deletePublisher(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.PUBLISHER_HAS_BOOKS));
    }

    @Test
    @DisplayName("deletePublisher - Thành công xóa nhà xuất bản khi không có sách")
    void testDeletePublisher_Success() {
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(testPublisher));
        when(publisherRepository.countTotalBooksByPublisherId(1L)).thenReturn(0L);

        adminPublisherService.deletePublisher(1L);

        verify(publisherRepository).delete(testPublisher);
    }
}

