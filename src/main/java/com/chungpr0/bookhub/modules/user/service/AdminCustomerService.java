package com.chungpr0.bookhub.modules.user.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.user.dto.request.AdjustPointsRequest;
import com.chungpr0.bookhub.modules.user.dto.request.CustomerFilterRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerStatusRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerDetailResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerOrderHistoryResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerStatusResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerSummaryResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointAdjustmentResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointTransactionResponse;
import org.springframework.data.domain.Pageable;

public interface AdminCustomerService {

    PageResponse<CustomerSummaryResponse> getCustomers(CustomerFilterRequest filter, Pageable pageable);

    CustomerDetailResponse getCustomerDetail(Long id);

    CustomerSummaryResponse updateCustomer(Long id, UpdateCustomerRequest request);

    CustomerStatusResponse updateCustomerStatus(Long id, UpdateCustomerStatusRequest request);

    PageResponse<CustomerOrderHistoryResponse> getCustomerOrders(Long id, OrderStatus status, Pageable pageable);

    PointAdjustmentResponse adjustPoints(Long id, AdjustPointsRequest request, Long adminAccountId);

    PageResponse<PointTransactionResponse> getCustomerPointsHistory(Long id, Pageable pageable);
}

