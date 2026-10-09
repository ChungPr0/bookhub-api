package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorSummaryResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherDetailResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.service.PublisherService;
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
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PublisherCardResponse> getPublishers(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 60), Sort.by(Sort.Direction.ASC, "name"));
        Page<Publisher> publisherPage = publisherRepository.findAll(pageable);

        List<PublisherCardResponse> items = publisherPage.getContent().stream()
                .map(pub -> PublisherCardResponse.builder()
                        .id(pub.getId())
                        .name(pub.getName())
                        .slug(pub.getSlug())
                        .address(pub.getAddress())
                        .website(pub.getWebsite())
                        .bookCount(publisherRepository.countActiveBooksByPublisherId(pub.getId()))
                        .build())
                .toList();

        return PageResponse.of(items, publisherPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PublisherDetailResponse getPublisherBySlug(String slug) {
        Publisher publisher = publisherRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.PUBLISHER_NOT_FOUND));

        int bookCount = publisherRepository.countActiveBooksByPublisherId(publisher.getId());
        List<Book> topBooks = bookRepository.findTopBooksByPublisherId(publisher.getId(), PageRequest.of(0, 8));

        List<BookCardResponse> bookCards = topBooks.stream()
                .map(this::mapToBookCard)
                .toList();

        return PublisherDetailResponse.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .slug(publisher.getSlug())
                .address(publisher.getAddress())
                .website(publisher.getWebsite())
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
