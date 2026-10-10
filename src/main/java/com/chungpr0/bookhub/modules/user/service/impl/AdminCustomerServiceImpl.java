package com.chungpr0.bookhub.modules.user.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.user.dto.request.AdjustPointsRequest;
import com.chungpr0.bookhub.modules.user.dto.request.CustomerFilterRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerStatusRequest;
import com.chungpr0.bookhub.modules.user.dto.response.AddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerDetailResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerOrderHistoryResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerOrderStatsResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerStatusResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerSummaryResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointAdjustmentResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointTransactionResponse;
import com.chungpr0.bookhub.modules.user.dto.response.TierProgressResponse;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.modules.user.repository.specification.CustomerSpecification;
import com.chungpr0.bookhub.modules.user.repository.specification.PointTransactionSpecification;
import com.chungpr0.bookhub.modules.user.service.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private static final long SILVER_THRESHOLD = 2_000_000L;
    private static final long GOLD_THRESHOLD = 10_000_000L;

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerSummaryResponse> getCustomers(CustomerFilterRequest filter, Pageable pageable) {
        Page<Customer> customerPage = customerRepository.findAll(CustomerSpecification.filter(filter), pageable);

        List<CustomerSummaryResponse> items = customerPage.getContent().stream()
                .map(customer -> {
                    long orderCount = orderRepository.countByCustomerId(customer.getId());
                    return CustomerSummaryResponse.fromEntity(customer, orderCount);
                })
                .toList();

        return PageResponse.of(items, customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerDetailResponse getCustomerDetail(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        List<Address> addressEntities = addressRepository.findByCustomerIdOrderByIsDefaultDescUpdatedAtDesc(id);
        List<AddressResponse> addresses = addressEntities.stream()
                .map(AddressResponse::fromEntity)
                .toList();

        long totalOrders = orderRepository.countByCustomerId(id);
        long completedOrders = orderRepository.countByCustomerIdAndStatus(id, OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByCustomerIdAndStatus(id, OrderStatus.CANCELLED);
        long returnedOrders = orderRepository.countByCustomerIdAndStatus(id, OrderStatus.RETURNED);
        Long avgOrderValue = (completedOrders > 0 && customer.getTotalSpent() != null)
                ? customer.getTotalSpent() / completedOrders
                : 0L;

        CustomerOrderStatsResponse orderStats = CustomerOrderStatsResponse.builder()
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .returnedOrders(returnedOrders)
                .avgOrderValue(avgOrderValue)
                .build();

        TierProgressResponse tierProgress = calculateTierProgress(customer.getTotalSpent());

        return CustomerDetailResponse.builder()
                .id(customer.getId())
                .accountId(customer.getAccount() != null ? customer.getAccount().getId() : null)
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .gender(customer.getGender())
                .birthday(customer.getBirthday())
                .avatarUrl(customer.getAvatarUrl())
                .tier(customer.getCustomerTier())
                .rewardPoints(customer.getRewardPoints())
                .totalSpent(customer.getTotalSpent())
                .status(customer.getAccount() != null ? customer.getAccount().getStatus() : null)
                .createdAt(customer.getCreatedAt())
                .addresses(addresses)
                .orderStats(orderStats)
                .tierProgress(tierProgress)
                .build();
    }

    @Override
    @Transactional
    public CustomerSummaryResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(customer.getEmail())
                    && customerRepository.existsByEmailAndIdNot(newEmail, id)) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
            }
            customer.setEmail(newEmail);
        }

        customer.setFullName(request.getFullName().trim());
        customer.setGender(request.getGender());
        customer.setBirthday(request.getBirthday());

        customerRepository.save(customer);

        long orderCount = orderRepository.countByCustomerId(id);
        return CustomerSummaryResponse.fromEntity(customer, orderCount);
    }

    @Override
    @Transactional
    public CustomerStatusResponse updateCustomerStatus(Long id, UpdateCustomerStatusRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Account account = customer.getAccount();
        if (account == null) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        account.setStatus(request.getStatus());

        if (request.getStatus() == AccountStatus.LOCKED) {
            account.setLockedReason(request.getReason());
            // Invalidate active JWTs across devices immediately
            account.setTokenVersion(account.getTokenVersion() + 1);
        } else {
            account.setLockedReason(null);
            account.setFailedLoginCount(0);
            account.setLoginLockedUntil(null);
        }

        accountRepository.save(account);

        return CustomerStatusResponse.builder()
                .id(customer.getId())
                .accountId(account.getId())
                .status(account.getStatus())
                .reason(account.getLockedReason())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerOrderHistoryResponse> getCustomerOrders(Long id, OrderStatus status, Pageable pageable) {
        if (!customerRepository.existsById(id)) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        Page<Order> orderPage = (status != null)
                ? orderRepository.findByCustomerIdAndStatus(id, status, pageable)
                : orderRepository.findByCustomerId(id, pageable);

        List<CustomerOrderHistoryResponse> items = orderPage.getContent().stream()
                .map(CustomerOrderHistoryResponse::fromEntity)
                .toList();

        return PageResponse.of(items, orderPage);
    }

    @Override
    @Transactional
    public PointAdjustmentResponse adjustPoints(Long id, AdjustPointsRequest request, Long adminAccountId) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        int adjustment = request.getPoints();
        int currentPoints = customer.getRewardPoints();

        if (adjustment < 0 && Math.abs(adjustment) > currentPoints) {
            throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
        }

        int newBalance = currentPoints + adjustment;
        customer.setRewardPoints(newBalance);
        customerRepository.save(customer);

        PointTransaction transaction = PointTransaction.builder()
                .customer(customer)
                .type(PointTransactionType.ADJUST)
                .points(adjustment)
                .balanceAfter(newBalance)
                .note(request.getReason().trim())
                .createdBy(adminAccountId)
                .build();

        PointTransaction savedTx = pointTransactionRepository.save(transaction);

        return PointAdjustmentResponse.builder()
                .id(savedTx.getId())
                .customerId(customer.getId())
                .points(savedTx.getPoints())
                .balanceAfter(savedTx.getBalanceAfter())
                .reason(savedTx.getNote())
                .createdBy(savedTx.getCreatedBy())
                .createdAt(savedTx.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PointTransactionResponse> getCustomerPointsHistory(Long id, Pageable pageable) {
        if (!customerRepository.existsById(id)) {
            throw new AppException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        Page<PointTransaction> txPage = pointTransactionRepository.findAll(
                PointTransactionSpecification.filter(id, null),
                pageable
        );

        List<PointTransactionResponse> items = txPage.getContent().stream()
                .map(tx -> PointTransactionResponse.builder()
                        .id(tx.getId())
                        .type(tx.getType())
                        .points(tx.getPoints())
                        .balanceAfter(tx.getBalanceAfter())
                        .orderId(tx.getOrderId())
                        .note(tx.getNote())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .toList();

        return PageResponse.of(items, txPage);
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
}

