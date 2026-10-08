package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorDetailResponse;

public interface AuthorService {

    PageResponse<AuthorCardResponse> getAuthors(int page, int size);

    AuthorDetailResponse getAuthorBySlug(String slug);
}

