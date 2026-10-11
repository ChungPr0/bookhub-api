package com.chungpr0.bookhub.modules.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookhub.order")
public class OrderProperties {

    /**
     * VNPay payment timeout duration in minutes. Default: 15 minutes.
     */
    private int vnpayTimeoutMinutes = 15;

    /**
     * Bank transfer payment timeout duration in hours. Default: 48 hours.
     */
    private int bankTransferTimeoutHours = 48;
}

