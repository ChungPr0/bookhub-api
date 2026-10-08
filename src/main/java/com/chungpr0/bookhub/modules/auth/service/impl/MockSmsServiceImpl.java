package com.chungpr0.bookhub.modules.auth.service.impl;

import com.chungpr0.bookhub.common.enums.OtpPurpose;
import com.chungpr0.bookhub.modules.auth.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MockSmsServiceImpl implements SmsService {

    @Override
    public void sendOtp(String phone, String otpCode, OtpPurpose purpose) {
        log.info("Sending SMS OTP to [{}] for purpose [{}]: {}", phone, purpose, otpCode);
    }
}

