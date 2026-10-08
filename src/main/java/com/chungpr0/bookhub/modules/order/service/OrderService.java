package com.chungpr0.bookhub.modules.order.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.order.dto.request.CheckoutPreviewRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CreateOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CustomerCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.response.CheckoutPreviewResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CreateOrderResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderStatusCountResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ReorderReportResponse;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface OrderService {

    CheckoutPreviewResponse previewCheckout(Long accountId, CheckoutPreviewRequest request);

    CreateOrderResponse createOrder(Long accountId, String idempotencyKey, CreateOrderRequest request, String ipAddress);

    PageResponse<CustomerOrderSummaryResponse> getCustomerOrders(
            Long accountId,
            OrderStatus status,
            String keyword,
            LocalDate createdFrom,
            LocalDate createdTo,
            Pageable pageable
    );

    OrderStatusCountResponse getCustomerStatusCounts(Long accountId);

    CustomerOrderDetailResponse getCustomerOrderDetail(Long accountId, String orderCode);

    CustomerOrderDetailResponse cancelOrder(Long accountId, String orderCode, CustomerCancelOrderRequest request);

    CustomerOrderDetailResponse confirmReceived(Long accountId, String orderCode);

    ReorderReportResponse reorder(Long accountId, String orderCode);
}

