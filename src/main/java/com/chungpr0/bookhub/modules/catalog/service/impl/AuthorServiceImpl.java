package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorSummaryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookCardResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuthorCardResponse> getAuthors(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 60), Sort.by(Sort.Direction.ASC, "name"));
        Page<Author> authorPage = authorRepository.findAll(pageable);

        List<AuthorCardResponse> items = authorPage.getContent().stream()
                .map(author -> AuthorCardResponse.builder()
                        .id(author.getId())
                        .name(author.getName())
                        .slug(author.getSlug())
                        .avatarUrl(author.getAvatarUrl())
                        .bookCount(authorRepository.countActiveBooksByAuthorId(author.getId()))
                        .build())
                .toList();

        return PageResponse.of(items, authorPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorDetailResponse getAuthorBySlug(String slug) {
        Author author = authorRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_FOUND));

        int bookCount = authorRepository.countActiveBooksByAuthorId(author.getId());
        List<Book> topBooks = bookRepository.findTopBooksByAuthorId(author.getId(), PageRequest.of(0, 8));

        List<BookCardResponse> bookCards = topBooks.stream()
                .map(this::mapToBookCard)
                .toList();

        return AuthorDetailResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .slug(author.getSlug())
                .biography(author.getBiography())
                .avatarUrl(author.getAvatarUrl())
                .bookCount(bookCount)
                .topBooks(bookCards)
                .build();
    }

    private BookCardResponse mapToBookCard(Book book) {
        List<AuthorSummaryResponse> authors = book.getAuthors() != null
                ? book.getAuthors().stream()
                .map(a -> AuthorSummaryResponse.builder().id(a.getId()).name(a.getName()).slug(a.getSlug()).build())
                .toList()
                : Collections.emptyList();

        return BookCardResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .thumbnailUrl(book.getThumbnailUrl())
                .authors(authors)
                .originalPrice(book.getOriginalPrice())
                .salePrice(book.getSalePrice())
                .discountPercent(book.getDiscountPercent())
                .avgRating(book.getAvgRating())
                .reviewCount(book.getReviewCount())
                .soldCount(book.getSoldCount())
                .stockStatus(book.getStockStatus())
                .isBestSeller(book.getSoldCount() >= 1000)
                .build();
    }
}
