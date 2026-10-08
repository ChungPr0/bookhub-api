package com.chungpr0.bookhub.modules.order.service.impl;

import com.chungpr0.bookhub.common.dto.PageMeta;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.order.dto.request.AdminBankTransferConfirmRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminRefundOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminReturnOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminUpdateOrderStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.OrderFilterRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderStatusHistoryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.NeedsAttentionResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderStatusCountResponse;
import com.chungpr0.bookhub.modules.order.dto.response.StaffOrAccountInfoResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.entity.OrderStatusHistory;
import com.chungpr0.bookhub.modules.order.entity.Payment;
import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.entity.VoucherUsage;
import com.chungpr0.bookhub.modules.order.enums.ActorType;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentTxnStatus;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
import com.chungpr0.bookhub.modules.order.repository.specification.OrderSpecification;
import com.chungpr0.bookhub.modules.order.service.AdminOrderService;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final long SILVER_THRESHOLD = 2_000_000L;
    private static final long GOLD_THRESHOLD = 5_000_000L;

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final AccountRepository accountRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminOrderSummaryResponse> searchOrders(OrderFilterRequest filter, Pageable pageable) {
        Page<Order> page = orderRepository.findAll(
                OrderSpecification.filter(
                        filter != null ? filter.getCustomerId() : null,
                        filter != null ? filter.getKeyword() : null,
                        filter != null ? filter.getStatus() : null,
                        filter != null ? filter.getPaymentStatus() : null,
                        filter != null ? filter.getPaymentMethod() : null,
                        filter != null ? filter.getCreatedFrom() : null,
                        filter != null ? filter.getCreatedTo() : null,
                        filter != null ? filter.getFinalAmountFrom() : null,
                        filter != null ? filter.getFinalAmountTo() : null
                ),
                pageable
        );

        List<AdminOrderSummaryResponse> items = page.getContent().stream()
                .map(orderMapper::toAdminOrderSummaryResponse)
                .toList();

        PageMeta meta = PageMeta.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        return PageResponse.of(items, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusCountResponse getAdminStatusCounts() {
        long all = orderRepository.count();
        long pending = orderRepository.countByStatus(OrderStatus.PENDING);
        long confirmed = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        long shipping = orderRepository.countByStatus(OrderStatus.SHIPPING);
        long completed = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long cancelled = orderRepository.countByStatus(OrderStatus.CANCELLED);
        long returned = orderRepository.countByStatus(OrderStatus.RETURNED);

        long unpaidBankTransfer = orderRepository.countByPaymentMethodCodeAndPaymentStatus(
                PaymentMethodCode.BANK_TRANSFER,
                PaymentStatus.UNPAID
        );
        long refundPending = orderRepository.countByPaymentStatus(PaymentStatus.REFUND_PENDING);

        NeedsAttentionResponse attention = NeedsAttentionResponse.builder()
                .unpaidBankTransfer(unpaidBankTransfer)
                .refundPending(refundPending)
                .build();

        return OrderStatusCountResponse.builder()
                .all(all)
                .pending(pending)
                .confirmed(confirmed)
                .shipping(shipping)
                .completed(completed)
                .cancelled(cancelled)
                .returned(returned)
                .needsAttention(attention)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getAdminOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return orderMapper.toAdminOrderDetailResponse(order, computeAdminAllowedActions(order));
    }

    @Override
    @Transactional
    public AdminOrderDetailResponse updateOrderStatus(
            Long accountId,
            Long orderId,
            AdminUpdateOrderStatusRequest request
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!Integer.valueOf(order.getVersion()).equals(request.getVersion())) {
            throw new AppException(ErrorCode.CONCURRENT_MODIFICATION);
        }

        OrderStatus target = request.getStatus();
        if (target == OrderStatus.CANCELLED || target == OrderStatus.RETURNED) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Để hủy hoặc hoàn trả đơn, vui lòng sử dụng API chuyên biệt");
        }

        OrderStatus current = order.getStatus();
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        // State Machine validation
        boolean validTransition = false;
        if (current == OrderStatus.PENDING && target == OrderStatus.CONFIRMED) {
            validTransition = true;
            order.setConfirmedAt(now);
        } else if (current == OrderStatus.CONFIRMED && target == OrderStatus.SHIPPING) {
            // Online order must be PAID before SHIPPING
            if ((order.getPaymentMethod().getCode() == PaymentMethodCode.VNPAY
                    || order.getPaymentMethod().getCode() == PaymentMethodCode.BANK_TRANSFER)
                    && order.getPaymentStatus() != PaymentStatus.PAID) {
                throw new AppException(
                        ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                        "Không thể giao hàng cho đơn thanh toán online chưa hoàn tất thanh toán"
                );
            }
            validTransition = true;
            order.setShippedAt(now);
        } else if (current == OrderStatus.SHIPPING && target == OrderStatus.COMPLETED) {
            validTransition = true;
            order.setCompletedAt(now);
            if (order.getPaymentMethod().getCode() == PaymentMethodCode.COD) {
                order.setPaymentStatus(PaymentStatus.PAID);
            }

            // Customer Loyalty Earning
            Customer customer = order.getCustomer();
            int earned = calculateEstimatedEarn(order.getFinalAmount(), order.getShippingFee(), customer.getCustomerTier());
            order.setPointsEarned(earned);

            customer.setRewardPoints(customer.getRewardPoints() + earned);
            customer.setTotalSpent(customer.getTotalSpent() + order.getFinalAmount());

            if (customer.getTotalSpent() >= GOLD_THRESHOLD) {
                customer.setCustomerTier(CustomerTier.GOLD);
            } else if (customer.getTotalSpent() >= SILVER_THRESHOLD) {
                customer.setCustomerTier(CustomerTier.SILVER);
            }
            customerRepository.save(customer);

            if (earned > 0) {
                PointTransaction tx = PointTransaction.builder()
                        .customer(customer)
                        .type(PointTransactionType.EARN)
                        .points(earned)
                        .balanceAfter(customer.getRewardPoints())
                        .orderId(order.getId())
                        .note("Tích điểm khi hoàn tất đơn hàng " + order.getOrderCode())
                        .build();
                pointTransactionRepository.save(tx);
            }
        }

        if (!validTransition) {
            throw new AppException(
                    ErrorCode.INVALID_ORDER_STATUS_TRANSITION,
                    "Không thể chuyển trạng thái từ " + current + " sang " + target
            );
        }

        order.setStatus(target);
        order = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(current)
                .toStatus(target)
                .note(StringUtils.hasText(request.getNote()) ? request.getNote().trim() : "Nhân viên cập nhật trạng thái đơn")
                .changedBy(accountId)
                .actorType(ActorType.STAFF)
                .build();
        orderStatusHistoryRepository.save(history);

        return orderMapper.toAdminOrderDetailResponse(order, computeAdminAllowedActions(order));
    }

    @Override
    @Transactional
    public AdminOrderDetailResponse cancelOrder(Long accountId, Long orderId, AdminCancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!Integer.valueOf(order.getVersion()).equals(request.getVersion())) {
            throw new AppException(ErrorCode.CONCURRENT_MODIFICATION);
        }

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.RETURNED) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED, "Đơn hàng ở trạng thái hiện tại không thể hủy");
        }

        OffsetDateTime now = DateTimeUtils.nowVietnam();
        OrderStatus fromStatus = order.getStatus();

        // 1. Restore book stock
        for (OrderDetail detail : order.getOrderDetails()) {
            Book book = detail.getBook();
            book.setStockQuantity(book.getStockQuantity() + detail.getQuantity());
            book.setSoldCount(Math.max(0, book.getSoldCount() - detail.getQuantity()));
            bookRepository.save(book);
        }

        // 2. Rollback voucher
        if (order.getVoucher() != null) {
            Voucher v = order.getVoucher();
            v.setUsedCount(Math.max(0, v.getUsedCount() - 1));
            voucherRepository.save(v);

            Optional<VoucherUsage> usageOpt = voucherUsageRepository.findByOrderId(order.getId());
            if (usageOpt.isPresent()) {
                VoucherUsage usage = usageOpt.get();
                usage.setReleasedAt(now);
                voucherUsageRepository.save(usage);
            }
        }

        // 3. Refund points
        if (order.getPointsUsed() > 0) {
            Customer customer = order.getCustomer();
            customer.setRewardPoints(customer.getRewardPoints() + order.getPointsUsed());
            customerRepository.save(customer);

            PointTransaction tx = PointTransaction.builder()
                    .customer(customer)
                    .type(PointTransactionType.REFUND)
                    .points(order.getPointsUsed())
                    .balanceAfter(customer.getRewardPoints())
                    .orderId(order.getId())
                    .note("Cửa hàng hoàn điểm do hủy đơn hàng " + order.getOrderCode())
                    .build();
            pointTransactionRepository.save(tx);
        }

        // 4. Update order state
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(now);
        order.setCancelledBy(ActorType.STAFF);
        order.setCancelReason(request.getReason().name());
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUND_PENDING);
        }

        order = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(OrderStatus.CANCELLED)
                .note("Cửa hàng hủy đơn: " + (StringUtils.hasText(request.getNote()) ? request.getNote() : request.getReason().name()))
                .changedBy(accountId)
                .actorType(ActorType.STAFF)
                .build();
        orderStatusHistoryRepository.save(history);

        return orderMapper.toAdminOrderDetailResponse(order, computeAdminAllowedActions(order));
    }

    @Override
    @Transactional
    public AdminOrderDetailResponse confirmBankTransferPayment(
            Long accountId,
            Long orderId,
            AdminBankTransferConfirmRequest request
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getPaymentMethod().getCode() != PaymentMethodCode.BANK_TRANSFER) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Đơn hàng này không sử dụng phương thức chuyển khoản ngân hàng");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

        if (!order.getFinalAmount().equals(request.getAmountReceived())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Số tiền xác nhận không khớp giá trị đơn hàng");
        }

        OffsetDateTime now = DateTimeUtils.nowVietnam();
        OffsetDateTime paidAt = request.getPaidAt() != null ? request.getPaidAt() : now;

        order.setPaymentStatus(PaymentStatus.PAID);
        OrderStatus fromStatus = order.getStatus();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setConfirmedAt(now);
        }

        order = orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethodCode.BANK_TRANSFER)
                .amount(request.getAmountReceived())
                .status(PaymentTxnStatus.SUCCESS)
                .transactionRef(request.getBankTransactionRef().trim())
                .providerTransactionNo(request.getBankTransactionRef().trim())
                .paidAt(paidAt)
                .rawResponse(request.getNote())
                .build();
        paymentRepository.save(payment);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(order.getStatus())
                .note("Kế toán xác nhận nhận tiền chuyển khoản: " + request.getBankTransactionRef())
                .changedBy(accountId)
                .actorType(ActorType.STAFF)
                .build();
        orderStatusHistoryRepository.save(history);

        return orderMapper.toAdminOrderDetailResponse(order, computeAdminAllowedActions(order));
    }

    @Override
    @Transactional
    public AdminOrderDetailResponse returnOrder(Long accountId, Long orderId, AdminReturnOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!Integer.valueOf(order.getVersion()).equals(request.getVersion())) {
            throw new AppException(ErrorCode.CONCURRENT_MODIFICATION);
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION, "Chỉ đơn hàng đã hoàn tất mới có thể xử lý hoàn trả");
        }

        OffsetDateTime now = DateTimeUtils.nowVietnam();
        if (order.getCompletedAt() != null && order.getCompletedAt().plusDays(7).isBefore(now)) {
            throw new AppException(ErrorCode.RETURN_WINDOW_EXPIRED);
        }

        OrderStatus fromStatus = order.getStatus();

        // 1. Revoke points earned
        Customer customer = order.getCustomer();
        if (order.getPointsEarned() > 0) {
            int currentPoints = customer.getRewardPoints();
            customer.setRewardPoints(Math.max(0, currentPoints - order.getPointsEarned()));

            PointTransaction tx = PointTransaction.builder()
                    .customer(customer)
                    .type(PointTransactionType.REVOKE)
                    .points(-order.getPointsEarned())
                    .balanceAfter(customer.getRewardPoints())
                    .orderId(order.getId())
                    .note("Thu hồi điểm do hoàn trả đơn hàng " + order.getOrderCode())
                    .build();
            pointTransactionRepository.save(tx);
        }

        // 2. Reduce total spent & re-evaluate tier
        customer.setTotalSpent(Math.max(0L, customer.getTotalSpent() - order.getFinalAmount()));
        if (customer.getTotalSpent() < SILVER_THRESHOLD) {
            customer.setCustomerTier(CustomerTier.BRONZE);
        } else if (customer.getTotalSpent() < GOLD_THRESHOLD) {
            customer.setCustomerTier(CustomerTier.SILVER);
        }
        customerRepository.save(customer);

        // 3. Restock if requested
        if (Boolean.TRUE.equals(request.getRestock())) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Book book = detail.getBook();
                book.setStockQuantity(book.getStockQuantity() + detail.getQuantity());
                bookRepository.save(book);
            }
        }

        order.setStatus(OrderStatus.RETURNED);
        order.setPaymentStatus(PaymentStatus.REFUND_PENDING);
        order = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(OrderStatus.RETURNED)
                .note("Tiếp nhận hoàn trả đơn hàng: " + request.getReason())
                .changedBy(accountId)
                .actorType(ActorType.STAFF)
                .build();
        orderStatusHistoryRepository.save(history);

        return orderMapper.toAdminOrderDetailResponse(order, computeAdminAllowedActions(order));
    }

    @Override
    @Transactional
    public AdminOrderDetailResponse refundOrder(
            Long accountId,
            Long orderId,
            String idempotencyKey,
            AdminRefundOrderRequest request
    ) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new AppException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getPaymentStatus() != PaymentStatus.REFUND_PENDING) {
            throw new AppException(ErrorCode.REFUND_NOT_APPLICABLE);
        }

        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order = orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethodCode.BANK_TRANSFER)
                .amount(-request.getRefundAmount())
                .status(PaymentTxnStatus.REFUNDED)
                .transactionRef(request.getRefundTransactionRef().trim())
                .providerTransactionNo(request.getRefundTransactionRef().trim())
                .paidAt(DateTimeUtils.nowVietnam())
                .rawResponse(request.getNote())
                .build();
        paymentRepository.save(payment);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(order.getStatus())
                .toStatus(order.getStatus())
                .note("Đã hoàn tiền " + request.getRefundAmount() + "đ: " + request.getNote())
                .changedBy(accountId)
                .actorType(ActorType.STAFF)
                .build();
        orderStatusHistoryRepository.save(history);

        return orderMapper.toAdminOrderDetailResponse(order, computeAdminAllowedActions(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminOrderStatusHistoryResponse> getOrderStatusHistories(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        List<OrderStatusHistory> histories = orderStatusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
        List<AdminOrderStatusHistoryResponse> responses = new ArrayList<>();

        for (OrderStatusHistory h : histories) {
            StaffOrAccountInfoResponse changedBy = null;
            if (h.getChangedBy() != null) {
                Optional<Account> accOpt = accountRepository.findById(h.getChangedBy());
                if (accOpt.isPresent()) {
                    Account acc = accOpt.get();
                    changedBy = StaffOrAccountInfoResponse.builder()
                            .id(acc.getId())
                            .fullName(acc.getUsername())
                            .role(acc.getRole().name())
                            .build();
                }
            }
            responses.add(orderMapper.toAdminOrderStatusHistoryResponse(h, changedBy));
        }

        return responses;
    }

    private List<String> computeAdminAllowedActions(Order order) {
        List<String> actions = new ArrayList<>();
        if (order.getStatus() == OrderStatus.PENDING) {
            actions.add("CONFIRM");
            actions.add("CANCEL");
            if (order.getPaymentMethod().getCode() == PaymentMethodCode.BANK_TRANSFER
                    && order.getPaymentStatus() == PaymentStatus.UNPAID) {
                actions.add("CONFIRM_PAYMENT");
            }
        } else if (order.getStatus() == OrderStatus.CONFIRMED) {
            actions.add("SHIP");
            actions.add("CANCEL");
        } else if (order.getStatus() == OrderStatus.SHIPPING) {
            actions.add("COMPLETE");
            actions.add("CANCEL");
        } else if (order.getStatus() == OrderStatus.COMPLETED) {
            OffsetDateTime now = DateTimeUtils.nowVietnam();
            if (order.getCompletedAt() != null && !order.getCompletedAt().plusDays(7).isBefore(now)) {
                actions.add("RETURN");
            }
        }
        if (order.getPaymentStatus() == PaymentStatus.REFUND_PENDING) {
            actions.add("REFUND");
        }
        return actions;
    }

    private int calculateEstimatedEarn(long finalAmount, long shippingFee, CustomerTier tier) {
        long netAmount = Math.max(0L, finalAmount - shippingFee);
        long baseUnits = netAmount / 10_000L;
        double multiplier = switch (tier) {
            case SILVER -> 1.2;
            case GOLD -> 1.5;
            default -> 1.0;
        };
        return (int) Math.floor(baseUnits * multiplier);
    }
}
