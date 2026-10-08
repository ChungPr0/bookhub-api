package com.chungpr0.bookhub.modules.order.service.impl;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.modules.order.dto.request.RecreatePaymentUrlRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdatePaymentMethodRequest;
import com.chungpr0.bookhub.modules.order.dto.response.PaymentMethodResponse;
import com.chungpr0.bookhub.modules.order.dto.response.RecreatePaymentUrlResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VnpayReturnResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderStatusHistory;
import com.chungpr0.bookhub.modules.order.entity.Payment;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.enums.ActorType;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentTxnStatus;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentMethodRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentRepository;
import com.chungpr0.bookhub.modules.order.service.PaymentService;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    @Value("${bookhub.vnpay.tmn-code:BOOKHUB1}")
    private String vnpTmnCode;

    @Value("${bookhub.vnpay.hash-secret:RAO4A828SX0Z7G3IUXH44C6SGYCVS9H2}")
    private String vnpHashSecret;

    @Value("${bookhub.vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpPayUrl;

    @Value("${bookhub.vnpay.return-url:https://bookhub.vn/checkout/result}")
    private String vnpReturnUrl;

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getActivePaymentMethods() {
        return paymentMethodRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(orderMapper::toPaymentMethodResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getAllPaymentMethods() {
        return paymentMethodRepository.findAllByOrderBySortOrderAsc().stream()
                .map(orderMapper::toPaymentMethodResponse)
                .toList();
    }

    @Override
    @Transactional
    public PaymentMethodResponse updatePaymentMethod(Long id, UpdatePaymentMethodRequest request) {
        PaymentMethod pm = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Phương thức thanh toán không tồn tại"));

        if (request.getIsActive() != null && !request.getIsActive() && pm.isActive()) {
            long activeCount = paymentMethodRepository.countByIsActiveTrue();
            if (activeCount <= 1) {
                throw new AppException(ErrorCode.LAST_PAYMENT_METHOD_PROTECTION);
            }
            pm.setActive(false);
        } else if (request.getIsActive() != null && request.getIsActive()) {
            pm.setActive(true);
        }

        if (StringUtils.hasText(request.getName())) {
            pm.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            pm.setDescription(request.getDescription().trim());
        }
        if (request.getBankName() != null) {
            pm.setBankName(request.getBankName().trim());
        }
        if (request.getAccountNumber() != null) {
            pm.setAccountNumber(request.getAccountNumber().trim());
        }
        if (request.getAccountName() != null) {
            pm.setAccountName(request.getAccountName().trim());
        }
        if (request.getTransferContentTemplate() != null) {
            pm.setTransferContentTemplate(request.getTransferContentTemplate().trim());
        }

        pm = paymentMethodRepository.save(pm);
        return orderMapper.toPaymentMethodResponse(pm);
    }

    @Override
    public String generateVnpayPaymentUrl(Order order, String ipAddress, String bankCode) {
        String clientIp = StringUtils.hasText(ipAddress) ? ipAddress : "127.0.0.1";
        OffsetDateTime now = DateTimeUtils.nowVietnam();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String createDate = now.format(formatter);
        String expireDate = now.plusMinutes(15).format(formatter);

        long amount = order.getFinalAmount() * 100L; // VNPAY takes amount in xu (x100)
        String txnRef = order.getOrderCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang BookHub " + order.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", clientIp);
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        if (StringUtils.hasText(bankCode)) {
            vnpParams.put("vnp_BankCode", bankCode);
        }

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnpParams.get(fieldName);
            if (StringUtils.hasText(fieldValue)) {
                String encodedKey = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII);
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);

                hashData.append(fieldName).append('=').append(encodedValue);
                query.append(encodedKey).append('=').append(encodedValue);

                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(vnpHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnpPayUrl + "?" + query;
    }

    @Override
    @Transactional
    public RecreatePaymentUrlResponse recreatePaymentUrl(
            Long accountId,
            String orderCode,
            RecreatePaymentUrlRequest request,
            String ipAddress
    ) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Order order = orderRepository.findByOrderCodeAndCustomerId(orderCode, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getPaymentMethod().getCode() != PaymentMethodCode.VNPAY) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Đơn hàng này không sử dụng phương thức thanh toán VNPAY");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Đơn hàng không ở trạng thái cho phép tạo lại thanh toán");
        }

        OffsetDateTime now = DateTimeUtils.nowVietnam();
        if (order.getPaymentExpiresAt() != null && now.isAfter(order.getPaymentExpiresAt())) {
            throw new AppException(ErrorCode.PAYMENT_EXPIRED);
        }

        String bankCode = request != null ? request.getBankCode() : null;
        String paymentUrl = generateVnpayPaymentUrl(order, ipAddress, bankCode);
        String txnRef = "TXN-" + order.getOrderCode() + "-V2";

        return RecreatePaymentUrlResponse.builder()
                .paymentUrl(paymentUrl)
                .transactionRef(txnRef)
                .amount(order.getFinalAmount())
                .expiresAt(order.getPaymentExpiresAt())
                .build();
    }

    @Override
    @Transactional
    public String processVnpayIpn(Map<String, String> params) {
        if (!verifyChecksum(params)) {
            return "{\"RspCode\":\"97\",\"Message\":\"Invalid Checksum\"}";
        }

        String txnRef = params.get("vnp_TxnRef");
        if (!StringUtils.hasText(txnRef)) {
            return "{\"RspCode\":\"01\",\"Message\":\"Order not found\"}";
        }

        // txnRef is either orderCode or orderCode-XXXX
        String orderCode = txnRef.contains("-") && txnRef.split("-").length > 2
                ? txnRef.substring(0, txnRef.lastIndexOf('-'))
                : txnRef;

        Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
        if (orderOpt.isEmpty()) {
            orderOpt = orderRepository.findByOrderCode(txnRef);
        }
        if (orderOpt.isEmpty()) {
            return "{\"RspCode\":\"01\",\"Message\":\"Order not found\"}";
        }

        Order order = orderOpt.get();

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return "{\"RspCode\":\"02\",\"Message\":\"Order already confirmed\"}";
        }

        String amountStr = params.get("vnp_Amount");
        if (StringUtils.hasText(amountStr)) {
            long receivedAmount = Long.parseLong(amountStr) / 100L;
            if (!order.getFinalAmount().equals(receivedAmount)) {
                return "{\"RspCode\":\"04\",\"Message\":\"Invalid Amount\"}";
            }
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        PaymentTxnStatus txnStatus = ("00".equals(responseCode) && "00".equals(transactionStatus))
                ? PaymentTxnStatus.SUCCESS
                : PaymentTxnStatus.FAILED;

        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethodCode.VNPAY)
                .amount(order.getFinalAmount())
                .status(txnStatus)
                .transactionRef(txnRef)
                .providerTransactionNo(params.get("vnp_TransactionNo"))
                .bankCode(params.get("vnp_BankCode"))
                .paidAt(DateTimeUtils.nowVietnam())
                .rawResponse(params.toString())
                .build();
        paymentRepository.save(payment);

        if (txnStatus == PaymentTxnStatus.SUCCESS) {
            order.setPaymentStatus(PaymentStatus.PAID);
            OrderStatus fromStatus = order.getStatus();
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setConfirmedAt(DateTimeUtils.nowVietnam());
            }
            orderRepository.save(order);

            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .fromStatus(fromStatus)
                    .toStatus(order.getStatus())
                    .note("Hệ thống tự động duyệt sau khi VNPAY báo thanh toán thành công")
                    .actorType(ActorType.SYSTEM)
                    .build();
            orderStatusHistoryRepository.save(history);
        }

        return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
    }

    @Override
    public VnpayReturnResponse processVnpayReturn(Map<String, String> params) {
        boolean validSignature = verifyChecksum(params);
        String txnRef = params.get("vnp_TxnRef");
        String orderCode = txnRef != null && txnRef.contains("-") && txnRef.split("-").length > 2
                ? txnRef.substring(0, txnRef.lastIndexOf('-'))
                : txnRef;
        String responseCode = params.get("vnp_ResponseCode");

        boolean isSuccess = validSignature && "00".equals(responseCode);

        return VnpayReturnResponse.builder()
                .orderCode(orderCode)
                .paymentResult(isSuccess ? "SUCCESS" : "FAILED")
                .paymentStatus(isSuccess ? PaymentStatus.PAID : PaymentStatus.UNPAID)
                .message(isSuccess ? "Thanh toán thành công qua VNPAY" : "Thanh toán không thành công hoặc bị hủy")
                .vnpResponseCode(responseCode)
                .build();
    }

    private boolean verifyChecksum(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (!StringUtils.hasText(vnpSecureHash)) {
            return false;
        }

        Map<String, String> fields = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("vnp_")
                    && !"vnp_SecureHash".equals(entry.getKey())
                    && !"vnp_SecureHashType".equals(entry.getKey())) {
                fields.put(entry.getKey(), entry.getValue());
            }
        }

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = fields.get(fieldName);
            if (StringUtils.hasText(fieldValue)) {
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII);
                hashData.append(fieldName).append('=').append(encodedValue);
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }

        String actualHash = hmacSHA512(vnpHashSecret, hashData.toString());
        return actualHash.equalsIgnoreCase(vnpSecureHash);
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Error generating HMAC SHA-512", ex);
            return "";
        }
    }
}

