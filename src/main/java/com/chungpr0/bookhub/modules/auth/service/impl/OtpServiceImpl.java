package com.chungpr0.bookhub.modules.auth.service.impl;

import com.chungpr0.bookhub.common.enums.OtpPurpose;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.common.util.HashUtils;
import com.chungpr0.bookhub.common.util.MaskingUtils;
import com.chungpr0.bookhub.modules.auth.dto.response.OtpResponse;
import com.chungpr0.bookhub.modules.auth.entity.Otp;
import com.chungpr0.bookhub.modules.auth.repository.OtpRepository;
import com.chungpr0.bookhub.modules.auth.service.OtpService;
import com.chungpr0.bookhub.modules.auth.service.SmsService;
import com.chungpr0.bookhub.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final SmsService smsService;
    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public OtpResponse requestOtp(String phone, OtpPurpose purpose) {
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        // Check recent OTP request cooldown (60 seconds)
        Optional<Otp> lastOtpOpt = otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose);
        if (lastOtpOpt.isPresent()) {
            Otp lastOtp = lastOtpOpt.get();
            long elapsedSeconds = Duration.between(lastOtp.getCreatedAt(), now).toSeconds();
            if (elapsedSeconds < jwtProperties.getOtpResendIntervalSeconds()) {
                long remainingWait = jwtProperties.getOtpResendIntervalSeconds() - elapsedSeconds;
                throw new AppException(
                        ErrorCode.OTP_REQUEST_TOO_FREQUENT,
                        "Vui lòng đợi 60 giây trước khi yêu cầu mã OTP mới",
                        Map.of("retryAfterSeconds", remainingWait),
                        remainingWait
                );
            }
        }

        // Check daily limit (5 requests per 24h)
        long count24h = otpRepository.countByPhoneAndPurposeAndCreatedAtAfter(phone, purpose, now.minusHours(24));
        if (count24h >= jwtProperties.getOtpDailyLimit()) {
            throw new AppException(
                    ErrorCode.OTP_DAILY_LIMIT_EXCEEDED,
                    "Bạn đã vượt quá số lần nhận mã OTP cho phép trong ngày",
                    Map.of("retryAfterSeconds", 3600L),
                    3600L
            );
        }

        // Invalidate previous active OTPs for this phone and purpose
        otpRepository.invalidatePreviousOtps(phone, purpose);

        // Generate 6-digit OTP code
        String otpCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        String otpHash = HashUtils.sha256(phone + ":" + otpCode + ":" + purpose.name());

        Otp otp = Otp.builder()
                .phone(phone)
                .purpose(purpose)
                .otpHash(otpHash)
                .expiresAt(now.plusSeconds(jwtProperties.getOtpExpirationSeconds()))
                .attemptCount(0)
                .isUsed(false)
                .createdAt(now)
                .build();

        otpRepository.save(otp);

        // Send OTP via SMS
        smsService.sendOtp(phone, otpCode, purpose);

        return OtpResponse.builder()
                .phone(MaskingUtils.maskPhone(phone))
                .otpExpiresInSeconds(jwtProperties.getOtpExpirationSeconds())
                .resendAfterSeconds(jwtProperties.getOtpResendIntervalSeconds())
                .build();
    }

    @Override
    @Transactional(noRollbackFor = AppException.class)
    public void verifyOtp(String phone, String otpCode, OtpPurpose purpose) {
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        Otp otp = otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(phone, purpose)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_EXPIRED));

        if (otp.isUsed() || otp.getExpiresAt().isBefore(now)) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (otp.getAttemptCount() >= jwtProperties.getOtpMaxAttempts()) {
            throw new AppException(ErrorCode.OTP_ATTEMPTS_EXCEEDED);
        }

        String expectedHash = HashUtils.sha256(phone + ":" + otpCode + ":" + purpose.name());
        if (!expectedHash.equals(otp.getOtpHash())) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otpRepository.save(otp);

            int remainingAttempts = Math.max(0, jwtProperties.getOtpMaxAttempts() - otp.getAttemptCount());
            if (remainingAttempts == 0) {
                throw new AppException(ErrorCode.OTP_ATTEMPTS_EXCEEDED);
            }
            throw new AppException(ErrorCode.OTP_INVALID, "Mã xác thực OTP không chính xác", Map.of("remainingAttempts", remainingAttempts));
        }

        otp.setUsed(true);
        otpRepository.save(otp);
    }
}

