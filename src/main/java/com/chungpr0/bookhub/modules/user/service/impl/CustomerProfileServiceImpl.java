package com.chungpr0.bookhub.modules.user.service.impl;

import com.chungpr0.bookhub.common.dto.FieldErrorItem;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.exception.ValidationException;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateProfileRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerProfileResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointTransactionResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointsOverviewResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointsSummaryResponse;
import com.chungpr0.bookhub.modules.user.dto.response.TierProgressResponse;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import com.chungpr0.bookhub.modules.user.repository.specification.PointTransactionSpecification;
import com.chungpr0.bookhub.modules.user.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProfileServiceImpl implements CustomerProfileService {

    private static final long SILVER_THRESHOLD = 2_000_000L;
    private static final long GOLD_THRESHOLD = 5_000_000L;
    private static final String CDN_AVATAR_PREFIX = "https://cdn.bookhub.vn/avatars/";
    private static final int POINT_VALUE_VND = 100;

    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Long accountId) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        return buildProfileResponse(customer);
    }

    @Override
    @Transactional
    public CustomerProfileResponse updateProfile(Long accountId, UpdateProfileRequest request) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // Validate avatarUrl domain if provided
        if (StringUtils.hasText(request.getAvatarUrl())) {
            String avatarUrl = request.getAvatarUrl().trim();
            if (!avatarUrl.startsWith(CDN_AVATAR_PREFIX)) {
                throw new ValidationException(
                        "Dữ liệu không hợp lệ, vui lòng kiểm tra lại",
                        List.of(new FieldErrorItem(
                                "avatarUrl",
                                "INVALID_FORMAT",
                                "Đường dẫn ảnh đại diện phải thuộc hệ thống CDN BookHub (" + CDN_AVATAR_PREFIX + ")",
                                avatarUrl
                        ))
                );
            }
            customer.setAvatarUrl(avatarUrl);
        } else {
            customer.setAvatarUrl(null);
        }

        // Validate email uniqueness across customers and staff
        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase();
            if (!email.equalsIgnoreCase(customer.getEmail())) {
                if (customerRepository.existsByEmailAndIdNot(email, customer.getId())
                        || staffRepository.existsByEmail(email)) {
                    throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
                }
            }
            customer.setEmail(email);
        } else {
            customer.setEmail(null);
        }

        customer.setFullName(request.getFullName().trim());
        customer.setGender(request.getGender());
        customer.setBirthday(request.getBirthday());

        Customer saved = customerRepository.save(customer);
        return buildProfileResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PointsOverviewResponse getPoints(Long accountId, PointTransactionType type, Pageable pageable) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        TierProgressResponse progress = calculateTierProgress(customer.getTotalSpent());
        CustomerTier currentTier = progress.getCurrentTier();
        double multiplier = getTierMultiplier(currentTier);

        PointsSummaryResponse summary = PointsSummaryResponse.builder()
                .rewardPoints(customer.getRewardPoints())
                .pointValueVnd(POINT_VALUE_VND)
                .equivalentValueVnd((long) customer.getRewardPoints() * POINT_VALUE_VND)
                .tier(currentTier)
                .tierMultiplier(multiplier)
                .totalSpent(customer.getTotalSpent())
                .tierProgress(progress)
                .build();

        Pageable effectivePageable = (pageable != null && pageable.getSort().isSorted())
                ? pageable
                : PageRequest.of(
                        pageable != null ? pageable.getPageNumber() : 0,
                        pageable != null ? pageable.getPageSize() : 15,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                );

        Page<PointTransaction> transactionPage = pointTransactionRepository.findAll(
                PointTransactionSpecification.filter(customer.getId(), type),
                effectivePageable
        );

        Page<PointTransactionResponse> responsePage = transactionPage.map(this::mapToPointTransactionResponse);

        return PointsOverviewResponse.builder()
                .summary(summary)
                .history(PageResponse.of(responsePage))
                .build();
    }

    private CustomerProfileResponse buildProfileResponse(Customer customer) {
        TierProgressResponse tierProgress = calculateTierProgress(customer.getTotalSpent());

        return CustomerProfileResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .gender(customer.getGender())
                .birthday(customer.getBirthday())
                .avatarUrl(customer.getAvatarUrl())
                .tier(tierProgress.getCurrentTier())
                .rewardPoints(customer.getRewardPoints())
                .totalSpent(customer.getTotalSpent())
                .tierProgress(tierProgress)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    private TierProgressResponse calculateTierProgress(Long totalSpent) {
        long spent = totalSpent != null ? totalSpent : 0L;

        if (spent < SILVER_THRESHOLD) {
            long remaining = Math.max(0L, SILVER_THRESHOLD - spent);
            int percent = (int) Math.min(100L, (spent * 100L) / SILVER_THRESHOLD);
            return TierProgressResponse.builder()
                    .currentTier(CustomerTier.BRONZE)
                    .nextTier(CustomerTier.SILVER)
                    .nextTierThreshold(SILVER_THRESHOLD)
                    .amountToNextTier(remaining)
                    .progressPercent(percent)
                    .build();
        } else if (spent < GOLD_THRESHOLD) {
            long remaining = Math.max(0L, GOLD_THRESHOLD - spent);
            long span = GOLD_THRESHOLD - SILVER_THRESHOLD;
            int percent = (int) Math.min(100L, ((spent - SILVER_THRESHOLD) * 100L) / span);
            return TierProgressResponse.builder()
                    .currentTier(CustomerTier.SILVER)
                    .nextTier(CustomerTier.GOLD)
                    .nextTierThreshold(GOLD_THRESHOLD)
                    .amountToNextTier(remaining)
                    .progressPercent(percent)
                    .build();
        } else {
            return TierProgressResponse.builder()
                    .currentTier(CustomerTier.GOLD)
                    .nextTier(null)
                    .nextTierThreshold(null)
                    .amountToNextTier(0L)
                    .progressPercent(100)
                    .build();
        }
    }

    private double getTierMultiplier(CustomerTier tier) {
        return switch (tier) {
            case SILVER -> 1.2;
            case GOLD -> 1.5;
            default -> 1.0;
        };
    }

    private PointTransactionResponse mapToPointTransactionResponse(PointTransaction tx) {
        return PointTransactionResponse.builder()
                .id(tx.getId())
                .type(tx.getType())
                .points(tx.getPoints())
                .balanceAfter(tx.getBalanceAfter())
                .orderId(tx.getOrderId())
                .note(tx.getNote())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}

