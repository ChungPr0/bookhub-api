package com.chungpr0.bookhub.modules.auth;

import com.chungpr0.bookhub.common.enums.OtpPurpose;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.common.util.HashUtils;
import com.chungpr0.bookhub.modules.auth.dto.response.OtpResponse;
import com.chungpr0.bookhub.modules.auth.entity.Otp;
import com.chungpr0.bookhub.modules.auth.repository.OtpRepository;
import com.chungpr0.bookhub.modules.auth.service.impl.OtpServiceImpl;
import com.chungpr0.bookhub.modules.auth.service.SmsService;
import com.chungpr0.bookhub.security.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private OtpServiceImpl otpService;

    private final String testPhone = "0988888888";

    @BeforeEach
    void setUp() {
        when(jwtProperties.getOtpResendIntervalSeconds()).thenReturn(60L);
        when(jwtProperties.getOtpDailyLimit()).thenReturn(5);
        when(jwtProperties.getOtpExpirationSeconds()).thenReturn(300L);
        when(jwtProperties.getOtpMaxAttempts()).thenReturn(5);
    }

    @Test
    @DisplayName("OTP-01: Yêu cầu gửi OTP thành công")
    void testRequestOtpSuccess() {
        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(testPhone, OtpPurpose.REGISTER))
                .thenReturn(Optional.empty());
        when(otpRepository.countByPhoneAndPurposeAndCreatedAtAfter(eq(testPhone), eq(OtpPurpose.REGISTER), any(OffsetDateTime.class)))
                .thenReturn(0L);

        OtpResponse response = otpService.requestOtp(testPhone, OtpPurpose.REGISTER);

        assertThat(response).isNotNull();
        assertThat(response.getPhone()).isEqualTo("098****888");
        assertThat(response.getOtpExpiresInSeconds()).isEqualTo(300L);
        assertThat(response.getResendAfterSeconds()).isEqualTo(60L);
        verify(smsService).sendOtp(eq(testPhone), any(String.class), eq(OtpPurpose.REGISTER));
        verify(otpRepository).save(any(Otp.class));
    }

    @Test
    @DisplayName("OTP-02: Bắt lỗi gửi quá nhanh khi chưa qua 60 giây giãn cách")
    void testRequestOtpCooldownViolated() {
        Otp recentOtp = Otp.builder()
                .phone(testPhone)
                .purpose(OtpPurpose.REGISTER)
                .createdAt(DateTimeUtils.nowVietnam().minusSeconds(30))
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(testPhone, OtpPurpose.REGISTER))
                .thenReturn(Optional.of(recentOtp));

        assertThatThrownBy(() -> otpService.requestOtp(testPhone, OtpPurpose.REGISTER))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OTP_REQUEST_TOO_FREQUENT);
    }

    @Test
    @DisplayName("OTP-03: Bắt lỗi khi đã vượt quá 5 lần gửi OTP trong 24h")
    void testRequestOtpDailyLimitExceeded() {
        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(testPhone, OtpPurpose.REGISTER))
                .thenReturn(Optional.empty());
        when(otpRepository.countByPhoneAndPurposeAndCreatedAtAfter(eq(testPhone), eq(OtpPurpose.REGISTER), any(OffsetDateTime.class)))
                .thenReturn(5L);

        assertThatThrownBy(() -> otpService.requestOtp(testPhone, OtpPurpose.REGISTER))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OTP_DAILY_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("OTP-04: Xác thực OTP thành công")
    void testVerifyOtpSuccess() {
        String code = "123456";
        String hash = HashUtils.sha256(testPhone + ":" + code + ":" + OtpPurpose.REGISTER.name());

        Otp otp = Otp.builder()
                .phone(testPhone)
                .purpose(OtpPurpose.REGISTER)
                .otpHash(hash)
                .attemptCount(0)
                .isUsed(false)
                .expiresAt(DateTimeUtils.nowVietnam().plusMinutes(3))
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(testPhone, OtpPurpose.REGISTER))
                .thenReturn(Optional.of(otp));

        otpService.verifyOtp(testPhone, code, OtpPurpose.REGISTER);

        assertThat(otp.isUsed()).isTrue();
        verify(otpRepository).save(otp);
    }

    @Test
    @DisplayName("OTP-05: Xác thực OTP hết hạn ném OTP_EXPIRED")
    void testVerifyOtpExpired() {
        Otp expiredOtp = Otp.builder()
                .phone(testPhone)
                .purpose(OtpPurpose.REGISTER)
                .isUsed(false)
                .expiresAt(DateTimeUtils.nowVietnam().minusMinutes(1))
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(testPhone, OtpPurpose.REGISTER))
                .thenReturn(Optional.of(expiredOtp));

        assertThatThrownBy(() -> otpService.verifyOtp(testPhone, "123456", OtpPurpose.REGISTER))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OTP_EXPIRED);
    }

    @Test
    @DisplayName("OTP-06: Nhập sai mã OTP tăng số lần sai và ném OTP_INVALID")
    void testVerifyOtpWrongCode() {
        String correctHash = HashUtils.sha256(testPhone + ":654321:" + OtpPurpose.REGISTER.name());

        Otp otp = Otp.builder()
                .phone(testPhone)
                .purpose(OtpPurpose.REGISTER)
                .otpHash(correctHash)
                .attemptCount(0)
                .isUsed(false)
                .expiresAt(DateTimeUtils.nowVietnam().plusMinutes(3))
                .build();

        when(otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(testPhone, OtpPurpose.REGISTER))
                .thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> otpService.verifyOtp(testPhone, "111111", OtpPurpose.REGISTER))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OTP_INVALID);

        assertThat(otp.getAttemptCount()).isEqualTo(1);
        verify(otpRepository).save(otp);
    }
}
