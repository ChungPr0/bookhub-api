package com.chungpr0.bookhub.modules.auth.service;

import com.chungpr0.bookhub.common.enums.OtpPurpose;
import com.chungpr0.bookhub.modules.auth.dto.response.OtpResponse;

public interface OtpService {

    OtpResponse requestOtp(String phone, OtpPurpose purpose);

    void verifyOtp(String phone, String otpCode, OtpPurpose purpose);
}

