package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminPublisherResponse;

public interface AdminPublisherService {

    PageResponse<AdminPublisherResponse> getAdminPublishers(String keyword, int page, int size, String sort);

    AdminPublisherDetailResponse getAdminPublisherDetail(Long id);

    AdminPublisherResponse createPublisher(CreatePublisherRequest request);

    AdminPublisherResponse updatePublisher(Long id, UpdatePublisherRequest request);

    void deletePublisher(Long id);
}

