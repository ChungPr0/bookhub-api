package com.chungpr0.bookhub.modules.auth;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.HashUtils;
import com.chungpr0.bookhub.modules.auth.dto.request.ChangePasswordRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordResetRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.LoginRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RefreshTokenRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterRequest;
import com.chungpr0.bookhub.modules.auth.dto.response.LoginResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.OtpResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.RegisterResponse;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.entity.PasswordResetToken;
import com.chungpr0.bookhub.modules.auth.entity.RefreshToken;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.auth.repository.PasswordResetTokenRepository;
import com.chungpr0.bookhub.modules.auth.repository.RefreshTokenRepository;
import com.chungpr0.bookhub.modules.auth.service.impl.AuthServiceImpl;
import com.chungpr0.bookhub.modules.auth.service.OtpService;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import com.chungpr0.bookhub.security.JwtProperties;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthServiceImpl authService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .username("0988888888")
                .passwordHash("$2a$10$hashedpassword")
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .failedLoginCount(0)
                .build();
    }

    @Test
    @DisplayName("AUTH-01: Yêu cầu OTP đăng ký thất bại khi số điện thoại đã tồn tại")
    void testRequestRegisterOtpPhoneAlreadyRegistered() {
        when(accountRepository.existsByUsername("0988888888")).thenReturn(true);

        RegisterOtpRequest request = RegisterOtpRequest.builder().phone("0988888888").build();

        assertThatThrownBy(() -> authService.requestRegisterOtp(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHONE_ALREADY_REGISTERED);
    }

    @Test
    @DisplayName("AUTH-02: Đăng ký thành công")
    void testRegisterSuccess() {
        when(customerRepository.existsByEmail("chung@gmail.com")).thenReturn(false);
        when(accountRepository.existsByUsername("0988888888")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        RegisterRequest request = RegisterRequest.builder()
                .phone("0988888888")
                .otpCode("123456")
                .fullName("Nguyễn Tiến Chung")
                .password("Password123!")
                .email("chung@gmail.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2002, 4, 3))
                .build();

        RegisterResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getPhone()).isEqualTo("0988888888");
        assertThat(response.getFullName()).isEqualTo("Nguyễn Tiến Chung");
        verify(accountRepository).save(any(Account.class));
        verify(customerRepository).save(any(Customer.class));
        verify(cartRepository).save(any());
    }

    @Test
    @DisplayName("AUTH-03: Đăng nhập thất bại khi số điện thoại không tồn tại")
    void testLoginPhoneNotFound() {
        when(accountRepository.findByUsername("0988888888")).thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder().phone("0988888888").password("Password123!").build();

        assertThatThrownBy(() -> authService.login(request, "agent", "127.0.0.1"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("AUTH-03: Đăng nhập thất bại khi tài khoản bị khóa bởi quản trị viên")
    void testLoginAccountLocked() {
        testAccount.setStatus(AccountStatus.LOCKED);
        testAccount.setLockedReason("Gian lận đơn hàng");
        when(accountRepository.findByUsername("0988888888")).thenReturn(Optional.of(testAccount));

        LoginRequest request = LoginRequest.builder().phone("0988888888").password("Password123!").build();

        assertThatThrownBy(() -> authService.login(request, "agent", "127.0.0.1"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_LOCKED);
    }

    @Test
    @DisplayName("AUTH-03: Đăng nhập thành công trả về token và thông tin người dùng")
    void testLoginSuccess() {
        when(accountRepository.findByUsername("0988888888")).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("Password123!", testAccount.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(eq(1L), eq(Role.CUSTOMER), eq(0))).thenReturn("access_token");
        when(jwtProperties.getAccessTokenExpirationSeconds()).thenReturn(1800L);
        when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(604800L);

        LoginRequest request = LoginRequest.builder().phone("0988888888").password("Password123!").build();

        LoginResponse response = authService.login(request, "Mozilla", "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).startsWith("rt_");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AUTH-04: Phát hiện Token Reuse Detection khi dùng Refresh Token đã bị thu hồi")
    void testRefreshTokenReuseDetection() {
        String token = "rt_reused_token";
        String tokenHash = HashUtils.sha256(token);

        RefreshToken revokedRt = RefreshToken.builder()
                .account(testAccount)
                .tokenHash(tokenHash)
                .familyId("family-uuid-1")
                .revokedAt(OffsetDateTime.now().minusHours(1))
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(revokedRt));

        RefreshTokenRequest request = RefreshTokenRequest.builder().refreshToken(token).build();

        assertThatThrownBy(() -> authService.refreshToken(request, "agent", "127.0.0.1"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_REUSED);

        verify(refreshTokenRepository).revokeAllByFamilyId(eq("family-uuid-1"), any(OffsetDateTime.class));
        assertThat(testAccount.getTokenVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("AUTH-08: Đổi mật khẩu thất bại khi mật khẩu hiện tại sai")
    void testChangePasswordWrongCurrentPassword() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("WrongPass123!", testAccount.getPasswordHash())).thenReturn(false);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongPass123!")
                .newPassword("NewPass456!")
                .build();

        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CURRENT_PASSWORD_INCORRECT);
    }

    @Test
    @DisplayName("AUTH-08: Đổi mật khẩu thất bại khi mật khẩu mới trùng mật khẩu cũ")
    void testChangePasswordSamePassword() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(passwordEncoder.matches("OldPass123!", testAccount.getPasswordHash())).thenReturn(true);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPass123!")
                .newPassword("OldPass123!")
                .build();

        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_REUSED);
    }

    @Test
    @DisplayName("AUTH-09: Quên mật khẩu chống User Enumeration (giả lập thành công khi số không tồn tại)")
    void testRequestForgotPasswordOtpAntiEnumeration() {
        when(accountRepository.findByUsername("0988888888")).thenReturn(Optional.empty());
        when(jwtProperties.getOtpExpirationSeconds()).thenReturn(300L);
        when(jwtProperties.getOtpResendIntervalSeconds()).thenReturn(60L);

        ForgotPasswordOtpRequest request = ForgotPasswordOtpRequest.builder().phone("0988888888").build();

        OtpResponse response = authService.requestForgotPasswordOtp(request);

        assertThat(response).isNotNull();
        assertThat(response.getPhone()).isEqualTo("098****888");
        assertThat(response.getOtpExpiresInSeconds()).isEqualTo(300L);
    }

    @Test
    @DisplayName("AUTH-11: Đặt lại mật khẩu thành công bằng resetToken")
    void testResetForgotPasswordSuccess() {
        String token = "prt_reset_token";
        String tokenHash = HashUtils.sha256(token);

        PasswordResetToken prt = PasswordResetToken.builder()
                .account(testAccount)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .usedAt(null)
                .build();

        when(passwordResetTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(prt));
        when(passwordEncoder.encode("NewStrong123!")).thenReturn("newHashed");

        ForgotPasswordResetRequest request = ForgotPasswordResetRequest.builder()
                .phone("0988888888")
                .resetToken(token)
                .newPassword("NewStrong123!")
                .build();

        authService.resetForgotPassword(request);

        assertThat(testAccount.getFailedLoginCount()).isEqualTo(0);
        assertThat(testAccount.getTokenVersion()).isEqualTo(1);
        assertThat(prt.getUsedAt()).isNotNull();
        verify(accountRepository).save(testAccount);
        verify(refreshTokenRepository).revokeAllByAccountId(eq(1L), any(OffsetDateTime.class));
    }
}

