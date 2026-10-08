package com.chungpr0.bookhub.modules.order.service;

import com.chungpr0.bookhub.modules.order.dto.request.RecreatePaymentUrlRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdatePaymentMethodRequest;
import com.chungpr0.bookhub.modules.order.dto.response.PaymentMethodResponse;
import com.chungpr0.bookhub.modules.order.dto.response.RecreatePaymentUrlResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VnpayReturnResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    List<PaymentMethodResponse> getActivePaymentMethods();

    List<PaymentMethodResponse> getAllPaymentMethods();

    PaymentMethodResponse updatePaymentMethod(Long id, UpdatePaymentMethodRequest request);

    String generateVnpayPaymentUrl(Order order, String ipAddress, String bankCode);

    RecreatePaymentUrlResponse recreatePaymentUrl(Long accountId, String orderCode, RecreatePaymentUrlRequest request, String ipAddress);

    String processVnpayIpn(Map<String, String> params);

    VnpayReturnResponse processVnpayReturn(Map<String, String> params);
}

