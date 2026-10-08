package com.chungpr0.bookhub.modules.order.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.order.dto.request.AdminBankTransferConfirmRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminRefundOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminReturnOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminUpdateOrderStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.OrderFilterRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderStatusHistoryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderStatusCountResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminOrderService {

    PageResponse<AdminOrderSummaryResponse> searchOrders(OrderFilterRequest filter, Pageable pageable);

    OrderStatusCountResponse getAdminStatusCounts();

    AdminOrderDetailResponse getAdminOrderDetail(Long orderId);

    AdminOrderDetailResponse updateOrderStatus(Long accountId, Long orderId, AdminUpdateOrderStatusRequest request);

    AdminOrderDetailResponse cancelOrder(Long accountId, Long orderId, AdminCancelOrderRequest request);

    AdminOrderDetailResponse confirmBankTransferPayment(Long accountId, Long orderId, AdminBankTransferConfirmRequest request);

    AdminOrderDetailResponse returnOrder(Long accountId, Long orderId, AdminReturnOrderRequest request);

    AdminOrderDetailResponse refundOrder(Long accountId, Long orderId, String idempotencyKey, AdminRefundOrderRequest request);

    List<AdminOrderStatusHistoryResponse> getOrderStatusHistories(Long orderId);
}

