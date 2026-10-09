package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.SlugUtils;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.repository.specification.PublisherSpecification;
import com.chungpr0.bookhub.modules.catalog.service.AdminPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPublisherServiceImpl implements AdminPublisherService {

    private final PublisherRepository publisherRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminPublisherResponse> getAdminPublishers(String keyword, int page, int size, String sort) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100), sortObj);
        Specification<Publisher> spec = PublisherSpecification.withKeyword(keyword);

        Page<Publisher> publisherPage = publisherRepository.findAll(spec, pageable);
        List<AdminPublisherResponse> items = publisherPage.getContent().stream()
                .map(publisher -> {
                    long bookCount = publisherRepository.countTotalBooksByPublisherId(publisher.getId());
                    return AdminPublisherResponse.builder()
                            .id(publisher.getId())
                            .name(publisher.getName())
                            .slug(publisher.getSlug())
                            .address(publisher.getAddress())
                            .website(publisher.getWebsite())
                            .bookCount(bookCount)
                            .createdAt(publisher.getCreatedAt())
                            .build();
                })
                .toList();

        return PageResponse.of(items, publisherPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPublisherDetailResponse getAdminPublisherDetail(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PUBLISHER_NOT_FOUND, "Không tìm thấy nhà xuất bản"));

        long bookCount = publisherRepository.countTotalBooksByPublisherId(publisher.getId());
        return AdminPublisherDetailResponse.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .slug(publisher.getSlug())
                .address(publisher.getAddress())
                .website(publisher.getWebsite())
                .bookCount(bookCount)
                .createdAt(publisher.getCreatedAt())
                .updatedAt(publisher.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminPublisherResponse createPublisher(CreatePublisherRequest request) {
        String trimmedName = request.getName().trim();
        if (publisherRepository.existsByName(trimmedName)) {
            throw new AppException(ErrorCode.PUBLISHER_NAME_DUPLICATE, "Tên nhà xuất bản đã tồn tại trong hệ thống");
        }

        String slug = generateUniqueSlug(trimmedName, null);

        Publisher publisher = Publisher.builder()
                .name(trimmedName)
                .slug(slug)
                .address(request.getAddress())
                .website(request.getWebsite())
                .build();

        Publisher saved = publisherRepository.save(publisher);
        return AdminPublisherResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .slug(saved.getSlug())
                .address(saved.getAddress())
                .website(saved.getWebsite())
                .bookCount(0)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminPublisherResponse updatePublisher(Long id, UpdatePublisherRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PUBLISHER_NOT_FOUND, "Không tìm thấy nhà xuất bản"));

        String trimmedName = request.getName().trim();
        if (publisherRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new AppException(ErrorCode.PUBLISHER_NAME_DUPLICATE, "Tên nhà xuất bản đã tồn tại trong hệ thống");
        }

        if (!publisher.getName().equals(trimmedName)) {
            publisher.setName(trimmedName);
            publisher.setSlug(generateUniqueSlug(trimmedName, id));
        }

        publisher.setAddress(request.getAddress());
        publisher.setWebsite(request.getWebsite());

        Publisher updated = publisherRepository.save(publisher);
        long bookCount = publisherRepository.countTotalBooksByPublisherId(updated.getId());

        return AdminPublisherResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .slug(updated.getSlug())
                .address(updated.getAddress())
                .website(updated.getWebsite())
                .bookCount(bookCount)
                .createdAt(updated.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deletePublisher(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PUBLISHER_NOT_FOUND, "Không tìm thấy nhà xuất bản"));

        long bookCount = publisherRepository.countTotalBooksByPublisherId(id);
        if (bookCount > 0) {
            throw new AppException(ErrorCode.PUBLISHER_HAS_BOOKS, "Không thể xóa: Nhà xuất bản này đang được liên kết với " + bookCount + " cuốn sách");
        }

        publisherRepository.delete(publisher);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        if ("name".equalsIgnoreCase(property)) {
            return Sort.by(direction, "name");
        }
        return Sort.by(direction, "createdAt");
    }

    private String generateUniqueSlug(String name, Long currentId) {
        String baseSlug = SlugUtils.toSlug(name);
        String candidateSlug = baseSlug;
        int counter = 2;

        while (isSlugConflict(candidateSlug, currentId)) {
            candidateSlug = baseSlug + "-" + counter;
            counter++;
        }
        return candidateSlug;
    }

    private boolean isSlugConflict(String slug, Long currentId) {
        if (currentId == null) {
            return publisherRepository.existsBySlug(slug);
        }
        return publisherRepository.existsBySlugAndIdNot(slug, currentId);
    }
}

