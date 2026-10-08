package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.request.BookSearchFilter;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookReviewResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSearchResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSuggestResponse;

import java.util.List;

public interface BookService {

    BookSearchResponse searchBooks(BookSearchFilter filter, int page, int size, String sort);

    List<BookCardResponse> getBestSellers(int limit, String categorySlug);

    List<BookCardResponse> getNewArrivals(int limit, String categorySlug);

    BookSuggestResponse suggest(String keyword);

    BookDetailResponse getBookDetail(String slug, Long currentAccountId);

    List<BookCardResponse> getRelatedBooks(String slug, int limit);

    PageResponse<BookReviewResponse> getBookReviews(String slug, Integer rating, Boolean hasContent, int page, int size, String sort);

    PageResponse<AdminBookResponse> getAdminBooks(String keyword, String status, Long categoryId, int page, int size);

    AdminBookDetailResponse getAdminBookDetail(Long id);
}

