package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.SlugUtils;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.specification.AuthorSpecification;
import com.chungpr0.bookhub.modules.catalog.service.AdminAuthorService;
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
public class AdminAuthorServiceImpl implements AdminAuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminAuthorResponse> getAdminAuthors(String keyword, int page, int size, String sort) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100), sortObj);
        Specification<Author> spec = AuthorSpecification.withKeyword(keyword);

        Page<Author> authorPage = authorRepository.findAll(spec, pageable);
        List<AdminAuthorResponse> items = authorPage.getContent().stream()
                .map(author -> {
                    long bookCount = authorRepository.countTotalBooksByAuthorId(author.getId());
                    return AdminAuthorResponse.builder()
                            .id(author.getId())
                            .name(author.getName())
                            .slug(author.getSlug())
                            .avatarUrl(author.getAvatarUrl())
                            .bookCount(bookCount)
                            .createdAt(author.getCreatedAt())
                            .build();
                })
                .toList();

        return PageResponse.of(items, authorPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAuthorDetailResponse getAdminAuthorDetail(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_FOUND, "Không tìm thấy thông tin tác giả"));

        long bookCount = authorRepository.countTotalBooksByAuthorId(author.getId());
        return AdminAuthorDetailResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .slug(author.getSlug())
                .biography(author.getBiography())
                .avatarUrl(author.getAvatarUrl())
                .bookCount(bookCount)
                .createdAt(author.getCreatedAt())
                .updatedAt(author.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminAuthorResponse createAuthor(CreateAuthorRequest request) {
        String trimmedName = request.getName().trim();
        String slug = generateUniqueSlug(trimmedName, null);

        Author author = Author.builder()
                .name(trimmedName)
                .slug(slug)
                .biography(request.getBiography())
                .avatarUrl(request.getAvatarUrl())
                .build();

        Author saved = authorRepository.save(author);
        return AdminAuthorResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .slug(saved.getSlug())
                .avatarUrl(saved.getAvatarUrl())
                .bookCount(0)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminAuthorResponse updateAuthor(Long id, UpdateAuthorRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_FOUND, "Không tìm thấy thông tin tác giả"));

        String trimmedName = request.getName().trim();
        if (!author.getName().equals(trimmedName)) {
            author.setName(trimmedName);
            author.setSlug(generateUniqueSlug(trimmedName, id));
        }

        author.setBiography(request.getBiography());
        author.setAvatarUrl(request.getAvatarUrl());

        Author updated = authorRepository.save(author);
        long bookCount = authorRepository.countTotalBooksByAuthorId(updated.getId());

        return AdminAuthorResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .slug(updated.getSlug())
                .avatarUrl(updated.getAvatarUrl())
                .bookCount(bookCount)
                .createdAt(updated.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_FOUND, "Không tìm thấy thông tin tác giả"));

        long bookCount = authorRepository.countTotalBooksByAuthorId(id);
        if (bookCount > 0) {
            throw new AppException(ErrorCode.AUTHOR_HAS_BOOKS, "Không thể xóa: Tác giả này đang được liên kết với " + bookCount + " cuốn sách");
        }

        authorRepository.delete(author);
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
            return authorRepository.existsBySlug(slug);
        }
        return authorRepository.existsBySlugAndIdNot(slug, currentId);
    }
}

