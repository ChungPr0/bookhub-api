package com.chungpr0.bookhub.modules.catalog.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewReplyRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewVisibilityRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminReviewResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CustomerReviewItemResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PendingReviewItemResponse;

public interface ReviewService {

    PageResponse<PendingReviewItemResponse> getPendingReviews(Long accountId, int page, int size);

    PageResponse<CustomerReviewItemResponse> getCustomerReviews(Long accountId, int page, int size);

    CustomerReviewItemResponse createReview(Long accountId, CreateReviewRequest request);

    CustomerReviewItemResponse updateReview(Long accountId, Long reviewId, UpdateReviewRequest request);

    void deleteReview(Long accountId, Long reviewId);

    PageResponse<AdminReviewResponse> getAdminReviews(String keyword, Long bookId, Integer rating, ReviewStatus status, Boolean hasReply, int page, int size, String sort);

    void updateReviewVisibility(Long reviewId, AdminReviewVisibilityRequest request);

    void replyReview(Long reviewId, AdminReviewReplyRequest request, Long staffAccountId);

    void deleteReviewReply(Long reviewId);
}

