package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminAuthorResponse;

public interface AdminAuthorService {

    PageResponse<AdminAuthorResponse> getAdminAuthors(String keyword, int page, int size, String sort);

    AdminAuthorDetailResponse getAdminAuthorDetail(Long id);

    AdminAuthorResponse createAuthor(CreateAuthorRequest request);

    AdminAuthorResponse updateAuthor(Long id, UpdateAuthorRequest request);

    void deleteAuthor(Long id);
}

