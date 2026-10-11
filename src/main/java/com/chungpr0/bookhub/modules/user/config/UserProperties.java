package com.chungpr0.bookhub.modules.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookhub.user")
public class UserProperties {

    /**
     * Maximum number of shipping addresses each customer is allowed to save. Default: 10.
     */
    private int maxAddresses = 10;
}

