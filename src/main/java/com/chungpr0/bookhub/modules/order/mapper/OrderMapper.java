package com.chungpr0.bookhub.modules.order.mapper;

import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderPricingResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderStatusHistoryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.BankInfoResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderCustomerInfoResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderFirstItemResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderItemDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderPricingResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderReceiverResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderTimelineResponse;
import com.chungpr0.bookhub.modules.order.dto.response.PaymentMethodResponse;
import com.chungpr0.bookhub.modules.order.dto.response.PaymentResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingConfigResponse;
import com.chungpr0.bookhub.modules.order.dto.response.StaffOrAccountInfoResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherUsageResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.entity.OrderStatusHistory;
import com.chungpr0.bookhub.modules.order.entity.Payment;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.entity.ShippingConfig;
import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.entity.VoucherUsage;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class OrderMapper {

    public PaymentMethodResponse toPaymentMethodResponse(PaymentMethod entity) {
        if (entity == null) {
            return null;
        }

        BankInfoResponse bankInfo = null;
        if (entity.getCode() == PaymentMethodCode.BANK_TRANSFER && entity.getAccountNumber() != null) {
            bankInfo = BankInfoResponse.builder()
                    .bankName(entity.getBankName())
                    .accountNumber(entity.getAccountNumber())
                    .accountName(entity.getAccountName())
                    .transferContentTemplate(entity.getTransferContentTemplate())
                    .build();
        }

        return PaymentMethodResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.isActive())
                .sortOrder(entity.getSortOrder())
                .bankInfo(bankInfo)
                .build();
    }

    public VoucherResponse toVoucherResponse(Voucher entity, OffsetDateTime now, Long estimatedDiscount) {
        if (entity == null) {
            return null;
        }

        return VoucherResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .discountType(entity.getDiscountType())
                .discountValue(entity.getDiscountValue())
                .maxDiscountAmount(entity.getMaxDiscountAmount())
                .minOrderAmount(entity.getMinOrderAmount())
                .usageLimit(entity.getUsageLimit())
                .usageLimitPerCustomer(entity.getUsageLimitPerCustomer())
                .usedCount(entity.getUsedCount())
                .startDate(entity.getStartDate())
                .expirationDate(entity.getExpirationDate())
                .status(entity.getStatus())
                .state(entity.computeState(now))
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .estimatedDiscount(estimatedDiscount != null ? estimatedDiscount : 0L)
                .build();
    }

    public CustomerOrderSummaryResponse toCustomerOrderSummaryResponse(Order order) {
        if (order == null) {
            return null;
        }

        int itemCount = order.getOrderDetails() != null ? order.getOrderDetails().size() : 0;
        int totalQuantity = 0;
        OrderFirstItemResponse firstItem = null;

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail d : order.getOrderDetails()) {
                totalQuantity += d.getQuantity();
            }
            OrderDetail first = order.getOrderDetails().get(0);
            firstItem = OrderFirstItemResponse.builder()
                    .bookId(first.getBook() != null ? first.getBook().getId() : null)
                    .title(first.getBookTitle())
                    .thumbnailUrl(first.getThumbnailUrl())
                    .build();
        }

        return CustomerOrderSummaryResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().getCode() : null)
                .itemCount(itemCount)
                .totalQuantity(totalQuantity)
                .firstItem(firstItem)
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public CustomerOrderDetailResponse toCustomerOrderDetailResponse(Order order, List<String> allowedActions) {
        if (order == null) {
            return null;
        }

        List<OrderItemDetailResponse> itemResponses = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                itemResponses.add(toOrderItemDetailResponse(d));
            }
        }

        List<OrderTimelineResponse> timeline = new ArrayList<>();
        if (order.getStatusHistories() != null) {
            for (OrderStatusHistory h : order.getStatusHistories()) {
                timeline.add(toOrderTimelineResponse(h));
            }
        }

        long subtotal = order.getSubtotalAmount() != null ? order.getSubtotalAmount() : 0L;
        long voucherDiscount = order.getVoucherDiscount() != null ? order.getVoucherDiscount() : 0L;
        long pointsDiscount = order.getPointsDiscount() != null ? order.getPointsDiscount() : 0L;
        long shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0L;
        long finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : 0L;
        int pointsUsed = order.getPointsUsed();

        OrderPricingResponse pricing = OrderPricingResponse.builder()
                .subtotal(subtotal)
                .voucherCode(order.getVoucher() != null ? order.getVoucher().getCode() : null)
                .voucherDiscount(voucherDiscount)
                .pointsUsed(pointsUsed)
                .pointsDiscount(pointsDiscount)
                .shippingFee(shippingFee)
                .freeShippingThreshold(300000L)
                .amountToFreeShipping(Math.max(0L, 300000L - (subtotal - voucherDiscount)))
                .finalAmount(finalAmount)
                .build();

        OrderReceiverResponse receiver = OrderReceiverResponse.builder()
                .name(order.getReceiverName())
                .phone(order.getReceiverPhone())
                .address(order.getShippingAddress())
                .build();

        return CustomerOrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(toPaymentMethodResponse(order.getPaymentMethod()))
                .paymentExpiresAt(order.getPaymentExpiresAt())
                .receiver(receiver)
                .items(itemResponses)
                .pricing(pricing)
                .allowedActions(allowedActions != null ? allowedActions : List.of())
                .timeline(timeline)
                .note(order.getNote())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    public AdminOrderSummaryResponse toAdminOrderSummaryResponse(Order order) {
        if (order == null) {
            return null;
        }

        int itemCount = order.getOrderDetails() != null ? order.getOrderDetails().size() : 0;
        int totalQuantity = 0;
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                totalQuantity += d.getQuantity();
            }
        }

        OrderCustomerInfoResponse customerInfo = null;
        if (order.getCustomer() != null) {
            customerInfo = OrderCustomerInfoResponse.builder()
                    .id(order.getCustomer().getId())
                    .fullName(order.getCustomer().getFullName())
                    .phone(order.getCustomer().getPhone())
                    .email(order.getCustomer().getEmail())
                    .tier(order.getCustomer().getCustomerTier())
                    .build();
        }

        return AdminOrderSummaryResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customer(customerInfo)
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().getCode() : null)
                .paymentExpiresAt(order.getPaymentExpiresAt())
                .itemCount(itemCount)
                .totalQuantity(totalQuantity)
                .finalAmount(order.getFinalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public AdminOrderDetailResponse toAdminOrderDetailResponse(Order order, List<String> allowedActions) {
        if (order == null) {
            return null;
        }

        List<OrderItemDetailResponse> itemResponses = new ArrayList<>();
        long totalCost = 0L;
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                OrderItemDetailResponse item = toOrderItemDetailResponse(d);
                itemResponses.add(item);
                if (d.getCostAmount() != null) {
                    totalCost += d.getCostAmount();
                }
            }
        }

        List<PaymentResponse> paymentResponses = new ArrayList<>();
        if (order.getPayments() != null) {
            for (Payment p : order.getPayments()) {
                paymentResponses.add(toPaymentResponse(p));
            }
        }

        long finalAmt = order.getFinalAmount() != null ? order.getFinalAmount() : 0L;
        long shipFee = order.getShippingFee() != null ? order.getShippingFee() : 0L;
        long grossProfit = Math.max(0L, finalAmt - shipFee - totalCost);

        AdminOrderPricingResponse pricing = AdminOrderPricingResponse.builder()
                .subtotal(order.getSubtotalAmount() != null ? order.getSubtotalAmount() : 0L)
                .voucherCode(order.getVoucher() != null ? order.getVoucher().getCode() : null)
                .voucherDiscount(order.getVoucherDiscount() != null ? order.getVoucherDiscount() : 0L)
                .pointsUsed(order.getPointsUsed())
                .pointsDiscount(order.getPointsDiscount() != null ? order.getPointsDiscount() : 0L)
                .shippingFee(shipFee)
                .finalAmount(finalAmt)
                .totalCost(totalCost)
                .estimatedGrossProfit(grossProfit)
                .build();

        OrderCustomerInfoResponse customerInfo = null;
        if (order.getCustomer() != null) {
            customerInfo = OrderCustomerInfoResponse.builder()
                    .id(order.getCustomer().getId())
                    .fullName(order.getCustomer().getFullName())
                    .phone(order.getCustomer().getPhone())
                    .email(order.getCustomer().getEmail())
                    .tier(order.getCustomer().getCustomerTier())
                    .build();
        }

        OrderReceiverResponse receiver = OrderReceiverResponse.builder()
                .name(order.getReceiverName())
                .phone(order.getReceiverPhone())
                .address(order.getShippingAddress())
                .build();

        return AdminOrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .version(order.getVersion())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(toPaymentMethodResponse(order.getPaymentMethod()))
                .customer(customerInfo)
                .receiver(receiver)
                .note(order.getNote())
                .items(itemResponses)
                .pricing(pricing)
                .payments(paymentResponses)
                .allowedActions(allowedActions != null ? allowedActions : List.of())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .completedAt(order.getCompletedAt())
                .cancelledAt(order.getCancelledAt())
                .cancelReason(order.getCancelReason())
                .build();
    }

    public OrderItemDetailResponse toOrderItemDetailResponse(OrderDetail detail) {
        if (detail == null) {
            return null;
        }

        Long grossProfit = null;
        if (detail.getLineTotal() != null && detail.getCostAmount() != null) {
            grossProfit = Math.max(0L, detail.getLineTotal() - detail.getCostAmount());
        }

        return OrderItemDetailResponse.builder()
                .id(detail.getId())
                .bookId(detail.getBook() != null ? detail.getBook().getId() : null)
                .title(detail.getBookTitle())
                .isbn(detail.getBookIsbn())
                .thumbnailUrl(detail.getThumbnailUrl())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .lineTotal(detail.getLineTotal())
                .costAmount(detail.getCostAmount())
                .grossProfit(grossProfit)
                .build();
    }

    public OrderTimelineResponse toOrderTimelineResponse(OrderStatusHistory history) {
        if (history == null) {
            return null;
        }

        return OrderTimelineResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .note(history.getNote())
                .timestamp(history.getCreatedAt())
                .actorType(history.getActorType())
                .build();
    }

    public AdminOrderStatusHistoryResponse toAdminOrderStatusHistoryResponse(
            OrderStatusHistory history,
            StaffOrAccountInfoResponse changedBy
    ) {
        if (history == null) {
            return null;
        }

        return AdminOrderStatusHistoryResponse.builder()
                .id(history.getId())
                .fromStatus(history.getFromStatus())
                .toStatus(history.getToStatus())
                .note(history.getNote())
                .actorType(history.getActorType())
                .changedBy(changedBy)
                .createdAt(history.getCreatedAt())
                .build();
    }

    public PaymentResponse toPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionRef(payment.getTransactionRef())
                .providerTransactionNo(payment.getProviderTransactionNo())
                .bankCode(payment.getBankCode())
                .paidAt(payment.getPaidAt())
                .build();
    }

    public VoucherUsageResponse toVoucherUsageResponse(VoucherUsage usage) {
        if (usage == null) {
            return null;
        }

        return VoucherUsageResponse.builder()
                .id(usage.getId())
                .voucherCode(usage.getVoucher() != null ? usage.getVoucher().getCode() : null)
                .orderId(usage.getOrder() != null ? usage.getOrder().getId() : null)
                .orderCode(usage.getOrder() != null ? usage.getOrder().getOrderCode() : null)
                .customerId(usage.getCustomer() != null ? usage.getCustomer().getId() : null)
                .customerName(usage.getCustomer() != null ? usage.getCustomer().getFullName() : null)
                .discountAmount(usage.getDiscountAmount())
                .usedAt(usage.getUsedAt())
                .releasedAt(usage.getReleasedAt())
                .build();
    }

    public ShippingConfigResponse toShippingConfigResponse(ShippingConfig config, StaffOrAccountInfoResponse updatedBy) {
        if (config == null) {
            return null;
        }

        return ShippingConfigResponse.builder()
                .standardBaseFee(config.getStandardBaseFee())
                .expressBaseFee(config.getExpressBaseFee())
                .sameDayBaseFee(config.getSameDayBaseFee())
                .freeShippingThreshold(config.getFreeShippingThreshold())
                .maxFreeShippingSubsidy(config.getMaxFreeShippingSubsidy())
                .standardMaxWeightGram(config.getStandardMaxWeightGram())
                .overweightUnitGram(config.getOverweightUnitGram())
                .overweightSurcharge(config.getOverweightSurcharge())
                .updatedAt(config.getUpdatedAt())
                .updatedBy(updatedBy)
                .build();
    }
}
