package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherDetailResponse;

public interface PublisherService {

    PageResponse<PublisherCardResponse> getPublishers(int page, int size);

    PublisherDetailResponse getPublisherBySlug(String slug);
}

