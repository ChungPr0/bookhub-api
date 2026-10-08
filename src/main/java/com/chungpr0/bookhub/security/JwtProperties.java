package com.chungpr0.bookhub.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookhub.jwt")
public class JwtProperties {

    /**
     * Secret key for HS256 signing (at least 256 bits).
     */
    private String secret = "4c696e685f6368756e675f626f6f6b6875625f7365637265745f6b65795f3235365f626974735f6d696e696d756d5f73697a6521";

    /**
     * Access token validity in seconds (default: 30 minutes = 1800s).
     */
    private long accessTokenExpirationSeconds = 1800;

    /**
     * Refresh token validity in seconds (default: 7 days = 604800s).
     */
    private long refreshTokenExpirationSeconds = 604800;

    /**
     * Password reset token validity in seconds (default: 10 minutes = 600s).
     */
    private long resetTokenExpirationSeconds = 600;

    /**
     * OTP validity in seconds (default: 5 minutes = 300s).
     */
    private long otpExpirationSeconds = 300;

    /**
     * Minimum wait time between 2 OTP requests in seconds (default: 60s).
     */
    private long otpResendIntervalSeconds = 60;

    /**
     * Maximum OTP requests allowed per phone per 24 hours (default: 5).
     */
    private int otpDailyLimit = 5;

    /**
     * Maximum failed OTP verification attempts before invalidation (default: 5).
     */
    private int otpMaxAttempts = 5;

    /**
     * Maximum failed login attempts before temporary lockout (default: 5).
     */
    private int loginMaxAttempts = 5;

    /**
     * Duration of temporary login lockout in seconds (default: 15 minutes = 900s).
     */
    private long loginLockDurationSeconds = 900;
}

