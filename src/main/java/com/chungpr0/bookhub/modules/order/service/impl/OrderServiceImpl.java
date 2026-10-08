package com.chungpr0.bookhub.modules.order.service.impl;

import com.chungpr0.bookhub.common.dto.PageMeta;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CartItemAvailability;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import com.chungpr0.bookhub.modules.cart.entity.CartItem;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.order.dto.request.CheckoutPreviewRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CreateOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CustomerCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.response.BankTransferInstructionResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CheckoutItemResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CheckoutPreviewResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CreateOrderResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderSummaryResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderPricingResponse;
import com.chungpr0.bookhub.modules.order.dto.response.OrderStatusCountResponse;
import com.chungpr0.bookhub.modules.order.dto.response.PointsPreviewResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ReorderItemReport;
import com.chungpr0.bookhub.modules.order.dto.response.ReorderReportResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherResultResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.entity.OrderStatusHistory;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.entity.VoucherUsage;
import com.chungpr0.bookhub.modules.order.enums.ActorType;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentMethodRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
import com.chungpr0.bookhub.modules.order.repository.specification.OrderSpecification;
import com.chungpr0.bookhub.modules.order.service.OrderService;
import com.chungpr0.bookhub.modules.order.service.PaymentService;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final long FREE_SHIPPING_THRESHOLD = 300_000L;
    private static final long STANDARD_SHIPPING_FEE = 30_000L;
    private static final int POINT_VALUE_VND = 100;
    private static final long SILVER_THRESHOLD = 2_000_000L;
    private static final long GOLD_THRESHOLD = 5_000_000L;

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PaymentService paymentService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse previewCheckout(Long accountId, CheckoutPreviewRequest request) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (request.getBookIds() == null || request.getBookIds().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy giỏ hàng"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        Map<Long, CartItem> cartItemMap = cartItems.stream()
                .collect(Collectors.toMap(ci -> ci.getBook().getId(), ci -> ci));

        for (Long bookId : request.getBookIds()) {
            if (!cartItemMap.containsKey(bookId)) {
                throw new AppException(ErrorCode.ITEMS_NOT_IN_CART);
            }
        }

        List<CheckoutItemResponse> items = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        long subtotal = 0L;

        for (Long bookId : request.getBookIds()) {
            CartItem cartItem = cartItemMap.get(bookId);
            Book book = cartItem.getBook();

            if (book.getStatus() != BookStatus.ACTIVE) {
                throw new AppException(ErrorCode.BOOK_NOT_AVAILABLE, "Sách '" + book.getTitle() + "' đã ngừng kinh doanh");
            }

            if (cartItem.getQuantity() > book.getStockQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Sách '" + book.getTitle() + "' không đủ tồn kho khả dụng");
            }

            long lineTotal = book.getSalePrice() * cartItem.getQuantity();
            subtotal += lineTotal;

            items.add(CheckoutItemResponse.builder()
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .thumbnailUrl(book.getThumbnailUrl())
                    .unitPrice(book.getSalePrice())
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .availability(CartItemAvailability.AVAILABLE)
                    .build());
        }

        // Voucher Calculation
        VoucherResultResponse voucherResult = evaluateVoucher(request.getVoucherCode(), customer.getId(), subtotal);
        long voucherDiscount = 0L;
        if (voucherResult.isApplied() && StringUtils.hasText(voucherResult.getCode())) {
            Optional<Voucher> vOpt = voucherRepository.findByCode(voucherResult.getCode());
            if (vOpt.isPresent()) {
                voucherDiscount = calculateVoucherDiscount(vOpt.get(), subtotal);
            }
        }

        // Shipping fee calculation (Free if subtotal - voucherDiscount >= 300k)
        long netSubtotal = Math.max(0L, subtotal - voucherDiscount);
        long shippingFee = (netSubtotal >= FREE_SHIPPING_THRESHOLD) ? 0L : STANDARD_SHIPPING_FEE;
        long amountToFreeShipping = Math.max(0L, FREE_SHIPPING_THRESHOLD - netSubtotal);

        // Points redeem calculation
        int pointsAvailable = customer.getRewardPoints();
        long maxDiscountFromPoints = (long) (netSubtotal * 0.5); // max 50%
        int maxRedeemablePoints = (int) Math.min((long) pointsAvailable, maxDiscountFromPoints / POINT_VALUE_VND);

        int pointsToUse = request.getPointsToUse() != null ? request.getPointsToUse() : 0;
        if (pointsToUse > pointsAvailable) {
            pointsToUse = pointsAvailable;
            warnings.add("Số điểm thưởng sử dụng vượt quá số dư hiện có, đã tự động điều chỉnh về " + pointsToUse + " điểm.");
        }
        if (pointsToUse > maxRedeemablePoints) {
            pointsToUse = maxRedeemablePoints;
            warnings.add("Số điểm thưởng sử dụng vượt quá mức tối đa 50% tiền sách (" + maxRedeemablePoints + " điểm).");
        }

        long pointsDiscount = (long) pointsToUse * POINT_VALUE_VND;
        long finalAmount = Math.max(0L, netSubtotal - pointsDiscount + shippingFee);

        int estimatedEarn = calculateEstimatedEarn(finalAmount, shippingFee, customer.getCustomerTier());

        OrderPricingResponse pricing = OrderPricingResponse.builder()
                .subtotal(subtotal)
                .voucherCode(voucherResult.isApplied() ? voucherResult.getCode() : null)
                .voucherDiscount(voucherDiscount)
                .pointsUsed(pointsToUse)
                .pointsDiscount(pointsDiscount)
                .shippingFee(shippingFee)
                .freeShippingThreshold(FREE_SHIPPING_THRESHOLD)
                .amountToFreeShipping(amountToFreeShipping)
                .finalAmount(finalAmount)
                .build();

        PointsPreviewResponse pointsPreview = PointsPreviewResponse.builder()
                .available(pointsAvailable)
                .maxRedeemable(maxRedeemablePoints)
                .pointValue(POINT_VALUE_VND)
                .estimatedEarn(estimatedEarn)
                .build();

        return CheckoutPreviewResponse.builder()
                .items(items)
                .pricing(pricing)
                .points(pointsPreview)
                .voucherResult(voucherResult)
                .warnings(warnings)
                .build();
    }

    @Override
    @Transactional
    public CreateOrderResponse createOrder(
            Long accountId,
            String idempotencyKey,
            CreateOrderRequest request,
            String ipAddress
    ) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new AppException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }

        // Idempotency check: if order exists with this key, return it
        Optional<Order> existingOrderOpt = orderRepository.findByIdempotencyKey(idempotencyKey.trim());
        if (existingOrderOpt.isPresent()) {
            Order existing = existingOrderOpt.get();
            CustomerOrderDetailResponse detail = orderMapper.toCustomerOrderDetailResponse(
                    existing,
                    computeCustomerAllowedActions(existing)
            );
            String payUrl = (existing.getPaymentMethod().getCode() == PaymentMethodCode.VNPAY
                    && existing.getPaymentStatus() == PaymentStatus.UNPAID)
                    ? paymentService.generateVnpayPaymentUrl(existing, ipAddress, null)
                    : null;
            BankTransferInstructionResponse bankInfo = (existing.getPaymentMethod().getCode() == PaymentMethodCode.BANK_TRANSFER)
                    ? buildBankTransferInstruction(existing)
                    : null;
            return CreateOrderResponse.builder()
                    .order(detail)
                    .paymentUrl(payUrl)
                    .bankTransferInstruction(bankInfo)
                    .build();
        }

        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        if (request.getBookIds() == null || request.getBookIds().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        // Address resolution & validation
        String receiverName;
        String receiverPhone;
        String shippingAddress;

        if (request.getAddressId() != null && request.getShippingAddress() != null) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Chỉ được chọn 1 trong 2: địa chỉ có sẵn hoặc địa chỉ mới");
        } else if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
            if (!address.getCustomer().getId().equals(customer.getId())) {
                throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
            }
            receiverName = address.getReceiverName();
            receiverPhone = address.getReceiverPhone();
            shippingAddress = address.getFullAddress();
        } else if (request.getShippingAddress() != null) {
            receiverName = request.getShippingAddress().getFullName().trim();
            receiverPhone = request.getShippingAddress().getPhone().trim();
            shippingAddress = request.getShippingAddress().toFullAddressString();
        } else {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Vui lòng cung cấp địa chỉ nhận hàng");
        }

        // Payment method validation
        PaymentMethod paymentMethod = paymentMethodRepository.findByCode(request.getPaymentMethod())
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_FAILED, "Phương thức thanh toán không tồn tại"));
        if (!paymentMethod.isActive()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Phương thức thanh toán hiện tạm dừng hoạt động");
        }

        // Validate items in cart and stock
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy giỏ hàng"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        Map<Long, CartItem> cartItemMap = cartItems.stream()
                .collect(Collectors.toMap(ci -> ci.getBook().getId(), ci -> ci));

        for (Long bookId : request.getBookIds()) {
            if (!cartItemMap.containsKey(bookId)) {
                throw new AppException(ErrorCode.ITEMS_NOT_IN_CART);
            }
        }

        long subtotal = 0L;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (Long bookId : request.getBookIds()) {
            CartItem cartItem = cartItemMap.get(bookId);
            Book book = cartItem.getBook();

            if (book.getStatus() != BookStatus.ACTIVE) {
                throw new AppException(ErrorCode.BOOK_NOT_AVAILABLE, "Sách '" + book.getTitle() + "' đã ngừng kinh doanh");
            }

            if (cartItem.getQuantity() > book.getStockQuantity()) {
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK, "Sách '" + book.getTitle() + "' không đủ tồn kho");
            }

            // Atomically decrement stock quantity and increment soldCount
            book.setStockQuantity(book.getStockQuantity() - cartItem.getQuantity());
            book.setSoldCount(book.getSoldCount() + cartItem.getQuantity());
            bookRepository.save(book);

            long lineTotal = book.getSalePrice() * cartItem.getQuantity();
            subtotal += lineTotal;

            long costAmount = (long) (book.getSalePrice() * 0.75); // FIFO estimate
            orderDetails.add(OrderDetail.builder()
                    .book(book)
                    .bookTitle(book.getTitle())
                    .bookIsbn(book.getIsbn())
                    .thumbnailUrl(book.getThumbnailUrl())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(book.getSalePrice())
                    .lineTotal(lineTotal)
                    .costAmount(costAmount)
                    .build());
        }

        // Voucher Validation & Consumption
        Voucher appliedVoucher = null;
        long voucherDiscount = 0L;
        if (StringUtils.hasText(request.getVoucherCode())) {
            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode().trim().toUpperCase())
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

            OffsetDateTime now = DateTimeUtils.nowVietnam();
            if (voucher.getStatus() != VoucherStatus.ACTIVE || now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getExpirationDate())) {
                throw new AppException(ErrorCode.VOUCHER_EXPIRED);
            }
            if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new AppException(ErrorCode.VOUCHER_USAGE_LIMIT_REACHED);
            }
            long usedByCustomer = voucherUsageRepository.countByVoucherIdAndCustomerIdAndReleasedAtIsNull(voucher.getId(), customer.getId());
            if (usedByCustomer >= voucher.getUsageLimitPerCustomer()) {
                throw new AppException(ErrorCode.VOUCHER_USAGE_LIMIT_REACHED);
            }
            if (subtotal < voucher.getMinOrderAmount()) {
                throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_NOT_MET);
            }

            voucherDiscount = calculateVoucherDiscount(voucher, subtotal);
            voucher.setUsedCount(voucher.getUsedCount() + 1);
            voucherRepository.save(voucher);
            appliedVoucher = voucher;
        }

        // Shipping fee calculation
        long netSubtotal = Math.max(0L, subtotal - voucherDiscount);
        long shippingFee = (netSubtotal >= FREE_SHIPPING_THRESHOLD) ? 0L : STANDARD_SHIPPING_FEE;

        // Points Redemption
        int pointsToUse = request.getPointsToUse() != null ? request.getPointsToUse() : 0;
        if (pointsToUse > 0) {
            if (customer.getRewardPoints() < pointsToUse) {
                throw new AppException(ErrorCode.INSUFFICIENT_POINTS);
            }
            long maxDiscountFromPoints = (long) (netSubtotal * 0.5);
            int maxRedeemablePoints = (int) (maxDiscountFromPoints / POINT_VALUE_VND);
            if (pointsToUse > maxRedeemablePoints) {
                pointsToUse = maxRedeemablePoints;
            }
        }
        long pointsDiscount = (long) pointsToUse * POINT_VALUE_VND;
        long calculatedFinalAmount = Math.max(0L, netSubtotal - pointsDiscount + shippingFee);

        // Price Slippage Check (BR-04, CHK-04)
        if (request.getExpectedFinalAmount() == null || calculatedFinalAmount != request.getExpectedFinalAmount().longValue()) {
            throw new AppException(ErrorCode.PRICE_CHANGED);
        }

        // Deduct customer points if used
        if (pointsToUse > 0) {
            customer.setRewardPoints(customer.getRewardPoints() - pointsToUse);
            customerRepository.save(customer);

            PointTransaction pointTx = PointTransaction.builder()
                    .customer(customer)
                    .type(PointTransactionType.REDEEM)
                    .points(-pointsToUse)
                    .balanceAfter(customer.getRewardPoints())
                    .note("Sử dụng điểm thanh toán đơn hàng")
                    .build();
            pointTransactionRepository.save(pointTx);
        }

        // Remove purchased items from cart
        cartItemRepository.deleteByCartIdAndBookIdIn(cart.getId(), request.getBookIds());

        // Generate unique order code
        OffsetDateTime now = DateTimeUtils.nowVietnam();
        String orderCode = generateOrderCode(now);

        OffsetDateTime paymentExpiresAt = null;
        if (request.getPaymentMethod() == PaymentMethodCode.VNPAY) {
            paymentExpiresAt = now.plusMinutes(15);
        } else if (request.getPaymentMethod() == PaymentMethodCode.BANK_TRANSFER) {
            paymentExpiresAt = now.plusHours(48);
        }

        Order order = Order.builder()
                .orderCode(orderCode)
                .customer(customer)
                .paymentMethod(paymentMethod)
                .voucher(appliedVoucher)
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .shippingAddress(shippingAddress)
                .note(request.getNote())
                .subtotalAmount(subtotal)
                .shippingFee(shippingFee)
                .voucherDiscount(voucherDiscount)
                .pointsUsed(pointsToUse)
                .pointsDiscount(pointsDiscount)
                .finalAmount(calculatedFinalAmount)
                .pointsEarned(0)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentExpiresAt(paymentExpiresAt)
                .idempotencyKey(idempotencyKey.trim())
                .version(0)
                .build();

        for (OrderDetail detail : orderDetails) {
            order.addOrderDetail(detail);
        }

        OrderStatusHistory history = OrderStatusHistory.builder()
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING)
                .note("Khách hàng tạo đơn hàng thành công")
                .actorType(ActorType.CUSTOMER)
                .build();
        order.addStatusHistory(history);

        order = orderRepository.save(order);

        // Record Voucher Usage
        if (appliedVoucher != null) {
            VoucherUsage usage = VoucherUsage.builder()
                    .voucher(appliedVoucher)
                    .customer(customer)
                    .order(order)
                    .discountAmount(voucherDiscount)
                    .usedAt(now)
                    .build();
            voucherUsageRepository.save(usage);
        }

        // Prepare return payload
        CustomerOrderDetailResponse detailResponse = orderMapper.toCustomerOrderDetailResponse(
                order,
                computeCustomerAllowedActions(order)
        );

        String paymentUrl = null;
        if (request.getPaymentMethod() == PaymentMethodCode.VNPAY) {
            paymentUrl = paymentService.generateVnpayPaymentUrl(order, ipAddress, null);
        }

        BankTransferInstructionResponse bankTransferInstruction = null;
        if (request.getPaymentMethod() == PaymentMethodCode.BANK_TRANSFER) {
            bankTransferInstruction = buildBankTransferInstruction(order);
        }

        return CreateOrderResponse.builder()
                .order(detailResponse)
                .paymentUrl(paymentUrl)
                .bankTransferInstruction(bankTransferInstruction)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerOrderSummaryResponse> getCustomerOrders(
            Long accountId,
            OrderStatus status,
            String keyword,
            LocalDate createdFrom,
            LocalDate createdTo,
            Pageable pageable
    ) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        List<OrderStatus> statuses = status != null ? List.of(status) : null;
        Page<Order> page = orderRepository.findAll(
                OrderSpecification.filter(
                        customer.getId(),
                        keyword,
                        statuses,
                        null,
                        null,
                        createdFrom,
                        createdTo,
                        null,
                        null
                ),
                pageable
        );

        List<CustomerOrderSummaryResponse> items = page.getContent().stream()
                .map(orderMapper::toCustomerOrderSummaryResponse)
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
    public OrderStatusCountResponse getCustomerStatusCounts(Long accountId) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        long all = orderRepository.countByCustomerId(customer.getId());
        long pending = orderRepository.countByCustomerIdAndStatus(customer.getId(), OrderStatus.PENDING);
        long confirmed = orderRepository.countByCustomerIdAndStatus(customer.getId(), OrderStatus.CONFIRMED);
        long shipping = orderRepository.countByCustomerIdAndStatus(customer.getId(), OrderStatus.SHIPPING);
        long completed = orderRepository.countByCustomerIdAndStatus(customer.getId(), OrderStatus.COMPLETED);
        long cancelled = orderRepository.countByCustomerIdAndStatus(customer.getId(), OrderStatus.CANCELLED);
        long returned = orderRepository.countByCustomerIdAndStatus(customer.getId(), OrderStatus.RETURNED);

        return OrderStatusCountResponse.builder()
                .all(all)
                .pending(pending)
                .confirmed(confirmed)
                .shipping(shipping)
                .completed(completed)
                .cancelled(cancelled)
                .returned(returned)
                .needsAttention(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderDetailResponse getCustomerOrderDetail(Long accountId, String orderCode) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Order order = orderRepository.findByOrderCodeAndCustomerId(orderCode, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return orderMapper.toCustomerOrderDetailResponse(order, computeCustomerAllowedActions(order));
    }

    @Override
    @Transactional
    public CustomerOrderDetailResponse cancelOrder(Long accountId, String orderCode, CustomerCancelOrderRequest request) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Order order = orderRepository.findByOrderCodeAndCustomerId(orderCode, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
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
            customer.setRewardPoints(customer.getRewardPoints() + order.getPointsUsed());
            customerRepository.save(customer);

            PointTransaction tx = PointTransaction.builder()
                    .customer(customer)
                    .type(PointTransactionType.REFUND)
                    .points(order.getPointsUsed())
                    .balanceAfter(customer.getRewardPoints())
                    .orderId(order.getId())
                    .note("Hoàn điểm do khách hàng hủy đơn hàng " + order.getOrderCode())
                    .build();
            pointTransactionRepository.save(tx);
        }

        // 4. Update order state
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(now);
        order.setCancelledBy(ActorType.CUSTOMER);
        order.setCancelReason(request.getReason().name());
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUND_PENDING);
        }

        order = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(OrderStatus.CANCELLED)
                .note("Khách hàng hủy đơn: " + (StringUtils.hasText(request.getReasonNote()) ? request.getReasonNote() : request.getReason().name()))
                .actorType(ActorType.CUSTOMER)
                .build();
        orderStatusHistoryRepository.save(history);

        return orderMapper.toCustomerOrderDetailResponse(order, computeCustomerAllowedActions(order));
    }

    @Override
    @Transactional
    public CustomerOrderDetailResponse confirmReceived(Long accountId, String orderCode) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Order order = orderRepository.findByOrderCodeAndCustomerId(orderCode, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION, "Chỉ có thể xác nhận đã nhận khi đơn đang ở trạng thái giao hàng (SHIPPING)");
        }

        OffsetDateTime now = DateTimeUtils.nowVietnam();
        OrderStatus fromStatus = order.getStatus();

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(now);
        if (order.getPaymentMethod().getCode() == PaymentMethodCode.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        // Points accumulation (BR-05)
        int earned = calculateEstimatedEarn(order.getFinalAmount(), order.getShippingFee(), customer.getCustomerTier());
        order.setPointsEarned(earned);

        customer.setRewardPoints(customer.getRewardPoints() + earned);
        customer.setTotalSpent(customer.getTotalSpent() + order.getFinalAmount());

        // Update Tier (BR-07)
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

        order = orderRepository.save(order);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(OrderStatus.COMPLETED)
                .note("Khách hàng xác nhận đã nhận được hàng")
                .actorType(ActorType.CUSTOMER)
                .build();
        orderStatusHistoryRepository.save(history);

        return orderMapper.toCustomerOrderDetailResponse(order, computeCustomerAllowedActions(order));
    }

    @Override
    @Transactional
    public ReorderReportResponse reorder(Long accountId, String orderCode) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Order order = orderRepository.findByOrderCodeAndCustomerId(orderCode, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().customer(customer).build()));

        List<ReorderItemReport> addedItems = new ArrayList<>();
        List<ReorderItemReport> skippedItems = new ArrayList<>();
        int totalAdded = 0;

        for (OrderDetail detail : order.getOrderDetails()) {
            Book book = detail.getBook();
            if (book == null || book.getStatus() != BookStatus.ACTIVE) {
                skippedItems.add(ReorderItemReport.builder()
                        .bookId(detail.getBook() != null ? detail.getBook().getId() : null)
                        .title(detail.getBookTitle())
                        .quantity(detail.getQuantity())
                        .reason("Sách đã ngừng kinh doanh")
                        .build());
                continue;
            }

            if (book.getStockQuantity() <= 0) {
                skippedItems.add(ReorderItemReport.builder()
                        .bookId(book.getId())
                        .title(book.getTitle())
                        .quantity(detail.getQuantity())
                        .reason("Sách tạm thời hết hàng")
                        .build());
                continue;
            }

            Optional<CartItem> cartItemOpt = cartItemRepository.findByCartIdAndBookId(cart.getId(), book.getId());
            int targetQty = detail.getQuantity();
            if (cartItemOpt.isPresent()) {
                CartItem ci = cartItemOpt.get();
                int newQty = Math.min(ci.getQuantity() + targetQty, Math.min(book.getStockQuantity(), 99));
                ci.setQuantity(newQty);
                cartItemRepository.save(ci);
            } else {
                int finalQty = Math.min(targetQty, Math.min(book.getStockQuantity(), 99));
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .book(book)
                        .quantity(finalQty)
                        .build();
                cartItemRepository.save(newItem);
            }

            addedItems.add(ReorderItemReport.builder()
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .quantity(targetQty)
                    .reason("Đã thêm thành công vào giỏ hàng")
                    .build());
            totalAdded += targetQty;
        }

        return ReorderReportResponse.builder()
                .addedItems(addedItems)
                .skippedItems(skippedItems)
                .totalAdded(totalAdded)
                .build();
    }

    private VoucherResultResponse evaluateVoucher(String voucherCode, Long customerId, long subtotal) {
        if (!StringUtils.hasText(voucherCode)) {
            return VoucherResultResponse.builder()
                    .applied(false)
                    .code(null)
                    .errorCode(null)
                    .errorMessage(null)
                    .build();
        }

        String code = voucherCode.trim().toUpperCase();
        Optional<Voucher> vOpt = voucherRepository.findByCode(code);
        if (vOpt.isEmpty()) {
            return VoucherResultResponse.builder()
                    .applied(false)
                    .code(code)
                    .errorCode("VOUCHER_NOT_FOUND")
                    .errorMessage("Mã voucher không tồn tại")
                    .build();
        }

        Voucher v = vOpt.get();
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        if (v.getStatus() != VoucherStatus.ACTIVE || now.isBefore(v.getStartDate())) {
            return VoucherResultResponse.builder()
                    .applied(false)
                    .code(code)
                    .errorCode("VOUCHER_NOT_YET_VALID")
                    .errorMessage("Mã voucher chưa đến thời gian áp dụng")
                    .build();
        }

        if (now.isAfter(v.getExpirationDate())) {
            return VoucherResultResponse.builder()
                    .applied(false)
                    .code(code)
                    .errorCode("VOUCHER_EXPIRED")
                    .errorMessage("Mã voucher đã hết hạn sử dụng")
                    .build();
        }

        if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
            return VoucherResultResponse.builder()
                    .applied(false)
                    .code(code)
                    .errorCode("VOUCHER_USAGE_LIMIT_REACHED")
                    .errorMessage("Mã voucher đã hết lượt sử dụng")
                    .build();
        }

        long count = voucherUsageRepository.countByVoucherIdAndCustomerIdAndReleasedAtIsNull(v.getId(), customerId);
        if (count >= v.getUsageLimitPerCustomer()) {
            return VoucherResultResponse.builder()
                    .applied(false)
                    .code(code)
                    .errorCode("VOUCHER_USAGE_LIMIT_REACHED")
                    .errorMessage("Bạn đã dùng hết lượt cho mã voucher này")
                    .build();
        }

        if (subtotal < v.getMinOrderAmount()) {
            return VoucherResultResponse.builder()
                    .applied(false)
                    .code(code)
                    .errorCode("VOUCHER_MIN_ORDER_NOT_MET")
                    .errorMessage("Đơn hàng chưa đạt giá trị tối thiểu " + v.getMinOrderAmount() + "đ")
                    .build();
        }

        return VoucherResultResponse.builder()
                .applied(true)
                .code(code)
                .errorCode(null)
                .errorMessage(null)
                .build();
    }

    private long calculateVoucherDiscount(Voucher voucher, long subtotal) {
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
            long discount = (subtotal * voucher.getDiscountValue()) / 100L;
            if (voucher.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, voucher.getMaxDiscountAmount());
            }
            return discount;
        } else {
            return Math.min(voucher.getDiscountValue(), subtotal);
        }
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

    private List<String> computeCustomerAllowedActions(Order order) {
        List<String> actions = new ArrayList<>();
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        if (order.getStatus() == OrderStatus.PENDING) {
            actions.add("CANCEL");
            if (order.getPaymentMethod().getCode() == PaymentMethodCode.VNPAY
                    && order.getPaymentStatus() == PaymentStatus.UNPAID) {
                if (order.getPaymentExpiresAt() == null || now.isBefore(order.getPaymentExpiresAt())) {
                    actions.add("PAY");
                }
            }
        } else if (order.getStatus() == OrderStatus.SHIPPING) {
            actions.add("CONFIRM_RECEIVED");
        } else if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            actions.add("REORDER");
        }
        return actions;
    }

    private BankTransferInstructionResponse buildBankTransferInstruction(Order order) {
        String bankName = "Vietcombank";
        String accountNumber = "0011004567890";
        String accountName = "NHA SACH BOOKHUB VIET NAM";
        String template = "BOOKHUB " + order.getOrderCode();

        PaymentMethod pm = order.getPaymentMethod();
        if (pm != null && StringUtils.hasText(pm.getBankName())) {
            bankName = pm.getBankName();
        }
        if (pm != null && StringUtils.hasText(pm.getAccountNumber())) {
            accountNumber = pm.getAccountNumber();
        }
        if (pm != null && StringUtils.hasText(pm.getAccountName())) {
            accountName = pm.getAccountName();
        }

        String qrUrl = String.format(
                "https://img.vietqr.io/image/970436-%s-compact2.png?amount=%d&addInfo=%s",
                accountNumber,
                order.getFinalAmount(),
                template
        );

        return BankTransferInstructionResponse.builder()
                .bankName(bankName)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .amount(order.getFinalAmount())
                .transferContent(template)
                .qrCodeUrl(qrUrl)
                .build();
    }

    private String generateOrderCode(OffsetDateTime now) {
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return "ORD-" + datePart + "-" + sb;
    }
}
