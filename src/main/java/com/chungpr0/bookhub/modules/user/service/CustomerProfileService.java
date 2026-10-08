package com.chungpr0.bookhub.modules.user.service;

import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateProfileRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerProfileResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointsOverviewResponse;
import org.springframework.data.domain.Pageable;

public interface CustomerProfileService {

    CustomerProfileResponse getProfile(Long accountId);

    CustomerProfileResponse updateProfile(Long accountId, UpdateProfileRequest request);

    PointsOverviewResponse getPoints(Long accountId, PointTransactionType type, Pageable pageable);
}

