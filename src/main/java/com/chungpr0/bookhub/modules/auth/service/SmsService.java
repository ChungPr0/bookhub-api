package com.chungpr0.bookhub.modules.auth.service;

import com.chungpr0.bookhub.common.enums.OtpPurpose;

public interface SmsService {

    void sendOtp(String phone, String otpCode, OtpPurpose purpose);
}

