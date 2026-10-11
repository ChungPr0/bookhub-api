package com.chungpr0.bookhub.modules.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookhub.vnpay")
public class VnpayProperties {

    /**
     * VNPay terminal code (merchant ID).
     */
    private String tmnCode = "BOOKHUB";

    /**
     * VNPay secure hash secret (must be provided via environment variables in production).
     */
    private String hashSecret = "";

    /**
     * VNPay payment gateway URL.
     */
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    /**
     * VNPay return URL after payment.
     */
    private String returnUrl = "https://your-domain.com/checkout/result";
}

