package com.chungpr0.bookhub.modules.auth.service.impl;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.OtpPurpose;
import com.chungpr0.bookhub.common.enums.Permission;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.common.util.HashUtils;
import com.chungpr0.bookhub.common.util.MaskingUtils;
import com.chungpr0.bookhub.common.util.PasswordUtils;
import com.chungpr0.bookhub.common.util.PhoneUtils;
import com.chungpr0.bookhub.modules.auth.dto.request.ChangePasswordRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordResetRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordVerifyRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.LoginRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.LogoutRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RefreshTokenRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterRequest;
import com.chungpr0.bookhub.modules.auth.dto.response.LoginResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.OtpResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.RefreshTokenResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.RegisterResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.ResetTokenResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.UserInfoResponse;
import com.chungpr0.bookhub.modules.auth.dto.response.UserSummaryResponse;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.entity.PasswordResetToken;
import com.chungpr0.bookhub.modules.auth.entity.RefreshToken;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.auth.repository.PasswordResetTokenRepository;
import com.chungpr0.bookhub.modules.auth.repository.RefreshTokenRepository;
import com.chungpr0.bookhub.modules.auth.service.AuthService;
import com.chungpr0.bookhub.modules.auth.service.OtpService;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import com.chungpr0.bookhub.security.JwtProperties;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final CartRepository cartRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    public OtpResponse requestRegisterOtp(RegisterOtpRequest request) {
        String phone = PhoneUtils.normalize(request.getPhone());
        if (accountRepository.existsByUsername(phone)) {
            throw new AppException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }
        return otpService.requestOtp(phone, OtpPurpose.REGISTER);
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String phone = PhoneUtils.normalize(request.getPhone());

        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim().toLowerCase();
            if (customerRepository.existsByEmail(email)) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
            }
        }

        if (accountRepository.existsByUsername(phone)) {
            throw new AppException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // Verify OTP
        otpService.verifyOtp(phone, request.getOtpCode(), OtpPurpose.REGISTER);

        OffsetDateTime now = DateTimeUtils.nowVietnam();

        // Create Account
        Account account = Account.builder()
                .username(phone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .failedLoginCount(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        accountRepository.save(account);

        // Create Customer
        Customer customer = Customer.builder()
                .account(account)
                .fullName(request.getFullName().trim())
                .phone(phone)
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim().toLowerCase() : null)
                .gender(request.getGender())
                .birthday(request.getBirthday())
                .rewardPoints(0)
                .totalSpent(0L)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(now)
                .updatedAt(now)
                .build();
        customerRepository.save(customer);

        // Create empty Cart
        Cart cart = Cart.builder()
                .customer(customer)
                .updatedAt(now)
                .build();
        cartRepository.save(cart);

        return RegisterResponse.builder()
                .phone(phone)
                .fullName(customer.getFullName())
                .build();
    }

    @Override
    @Transactional(noRollbackFor = AppException.class)
    public LoginResponse login(LoginRequest request, String userAgent, String ipAddress) {
        String phone = PhoneUtils.normalize(request.getPhone());
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new AppException(
                    ErrorCode.ACCOUNT_LOCKED,
                    account.getLockedReason() != null ? Map.of("reason", account.getLockedReason()) : null
            );
        }

        if (account.getLoginLockedUntil() != null && account.getLoginLockedUntil().isAfter(now)) {
            long retryAfter = Duration.between(now, account.getLoginLockedUntil()).toSeconds();
            throw new AppException(
                    ErrorCode.ACCOUNT_TEMPORARILY_LOCKED,
                    "Tài khoản tạm thời bị khóa do nhập sai mật khẩu nhiều lần. Vui lòng thử lại sau 15 phút",
                    Map.of(
                            "lockedUntil", DateTimeUtils.format(account.getLoginLockedUntil()),
                            "retryAfterSeconds", retryAfter
                    ),
                    retryAfter
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            account.setFailedLoginCount(account.getFailedLoginCount() + 1);
            if (account.getFailedLoginCount() >= jwtProperties.getLoginMaxAttempts()) {
                account.setLoginLockedUntil(now.plusSeconds(jwtProperties.getLoginLockDurationSeconds()));
                accountRepository.save(account);

                long retryAfter = jwtProperties.getLoginLockDurationSeconds();
                throw new AppException(
                        ErrorCode.ACCOUNT_TEMPORARILY_LOCKED,
                        "Tài khoản tạm thời bị khóa do nhập sai mật khẩu nhiều lần. Vui lòng thử lại sau 15 phút",
                        Map.of(
                                "lockedUntil", DateTimeUtils.format(account.getLoginLockedUntil()),
                                "retryAfterSeconds", retryAfter
                        ),
                        retryAfter
                );
            }
            accountRepository.save(account);

            int remaining = Math.max(0, jwtProperties.getLoginMaxAttempts() - account.getFailedLoginCount());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS, Map.of("remainingAttempts", remaining));
        }

        // Credentials matched - reset failed login counter
        account.setFailedLoginCount(0);
        account.setLoginLockedUntil(null);
        account.setLastLoginAt(now);
        accountRepository.save(account);

        boolean mustChangePassword = (account.getStatus() == AccountStatus.UNVERIFIED);

        // Generate Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getRole(), account.getTokenVersion());

        // Generate Refresh Token
        String rawRefreshToken = "rt_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String refreshTokenHash = HashUtils.sha256(rawRefreshToken);
        String familyId = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .account(account)
                .tokenHash(refreshTokenHash)
                .familyId(familyId)
                .expiresAt(now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .createdAt(now)
                .build();
        refreshTokenRepository.save(refreshToken);

        // Build User Summary
        UserSummaryResponse userSummary = buildUserSummary(account);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationSeconds())
                .refreshExpiresIn(jwtProperties.getRefreshTokenExpirationSeconds())
                .mustChangePassword(mustChangePassword)
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress) {
        String tokenHash = HashUtils.sha256(request.getRefreshToken());
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);
        if (tokenOpt.isEmpty()) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        RefreshToken currentToken = tokenOpt.get();

        // Token Reuse Detection
        if (currentToken.getRevokedAt() != null) {
            log.warn("Refresh token reuse detected for familyId: {}", currentToken.getFamilyId());
            refreshTokenRepository.revokeAllByFamilyId(currentToken.getFamilyId(), now);
            Account account = currentToken.getAccount();
            account.setTokenVersion(account.getTokenVersion() + 1);
            accountRepository.save(account);
            throw new AppException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        // Expiration check
        if (currentToken.getExpiresAt().isBefore(now)) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // Account status check
        Account account = currentToken.getAccount();
        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // Revoke current refresh token
        currentToken.setRevokedAt(now);
        refreshTokenRepository.save(currentToken);

        // Generate new Refresh Token with SAME familyId (Rotation)
        String newRawRefreshToken = "rt_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String newRefreshTokenHash = HashUtils.sha256(newRawRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .account(account)
                .tokenHash(newRefreshTokenHash)
                .familyId(currentToken.getFamilyId())
                .expiresAt(now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .createdAt(now)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        // Generate new Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getRole(), account.getTokenVersion());

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationSeconds())
                .refreshExpiresIn(jwtProperties.getRefreshTokenExpirationSeconds())
                .build();
    }

    @Override
    @Transactional
    public void logout(Long accountId, LogoutRequest request) {
        String tokenHash = HashUtils.sha256(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (token.getAccount().getId().equals(accountId)) {
                token.setRevokedAt(DateTimeUtils.nowVietnam());
                refreshTokenRepository.save(token);
            }
        });
    }

    @Override
    @Transactional
    public void logoutAll(Long accountId) {
        OffsetDateTime now = DateTimeUtils.nowVietnam();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        account.setTokenVersion(account.getTokenVersion() + 1);
        accountRepository.save(account);

        refreshTokenRepository.revokeAllByAccountId(accountId, now);
    }

    @Override
    public UserInfoResponse getCurrentUser(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String fullName = null;
        String email = null;
        String avatarUrl = null;

        if (account.getRole() == Role.CUSTOMER) {
            Customer customer = customerRepository.findByAccountId(accountId).orElse(null);
            if (customer != null) {
                fullName = customer.getFullName();
                email = customer.getEmail();
                avatarUrl = customer.getAvatarUrl();
            }
        } else {
            Staff staff = staffRepository.findByAccountId(accountId).orElse(null);
            if (staff != null) {
                fullName = staff.getFullName();
                email = staff.getEmail();
                avatarUrl = staff.getAvatarUrl();
            }
        }

        List<String> permissions = Permission.getPermissionsByRole(account.getRole());

        return UserInfoResponse.builder()
                .accountId(account.getId())
                .phone(account.getUsername())
                .role(account.getRole())
                .status(account.getStatus())
                .mustChangePassword(account.getStatus() == AccountStatus.UNVERIFIED)
                .fullName(fullName)
                .email(email)
                .avatarUrl(avatarUrl)
                .permissions(permissions)
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse changePassword(Long accountId, ChangePasswordRequest request) {
        if (!PasswordUtils.isValid(request.getNewPassword())) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Mật khẩu mới phải có từ 8-64 ký tự, gồm chữ hoa, chữ thường và chữ số"
            );
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.CURRENT_PASSWORD_INCORRECT);
        }

        if (passwordEncoder.matches(request.getNewPassword(), account.getPasswordHash())) {
            throw new AppException(ErrorCode.PASSWORD_REUSED);
        }

        OffsetDateTime now = DateTimeUtils.nowVietnam();

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        if (account.getStatus() == AccountStatus.UNVERIFIED) {
            account.setStatus(AccountStatus.ACTIVE);
        }
        account.setTokenVersion(account.getTokenVersion() + 1);
        accountRepository.save(account);

        // Revoke all existing refresh tokens
        refreshTokenRepository.revokeAllByAccountId(accountId, now);

        // Issue new tokens for current session
        String accessToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getRole(), account.getTokenVersion());
        String rawRefreshToken = "rt_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        RefreshToken refreshToken = RefreshToken.builder()
                .account(account)
                .tokenHash(HashUtils.sha256(rawRefreshToken))
                .familyId(UUID.randomUUID().toString())
                .expiresAt(now.plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()))
                .createdAt(now)
                .build();
        refreshTokenRepository.save(refreshToken);

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationSeconds())
                .refreshExpiresIn(jwtProperties.getRefreshTokenExpirationSeconds())
                .build();
    }

    @Override
    public OtpResponse requestForgotPasswordOtp(ForgotPasswordOtpRequest request) {
        String phone = PhoneUtils.normalize(request.getPhone());
        Optional<Account> accountOpt = accountRepository.findByUsername(phone);

        // Anti-User-Enumeration: simulate success if account does not exist
        if (accountOpt.isEmpty()) {
            return OtpResponse.builder()
                    .phone(MaskingUtils.maskPhone(phone))
                    .otpExpiresInSeconds(jwtProperties.getOtpExpirationSeconds())
                    .resendAfterSeconds(jwtProperties.getOtpResendIntervalSeconds())
                    .build();
        }

        Account account = accountOpt.get();
        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new AppException(
                    ErrorCode.ACCOUNT_LOCKED,
                    "Tài khoản của bạn đã bị khóa, không thể khôi phục mật khẩu"
            );
        }

        return otpService.requestOtp(phone, OtpPurpose.RESET_PASSWORD);
    }

    @Override
    @Transactional
    public ResetTokenResponse verifyForgotPasswordOtp(ForgotPasswordVerifyRequest request) {
        String phone = PhoneUtils.normalize(request.getPhone());

        otpService.verifyOtp(phone, request.getOtpCode(), OtpPurpose.RESET_PASSWORD);

        Account account = accountRepository.findByUsername(phone)
                .orElseThrow(() -> new AppException(ErrorCode.RESET_TOKEN_INVALID));

        OffsetDateTime now = DateTimeUtils.nowVietnam();
        String resetToken = "prt_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = HashUtils.sha256(resetToken);

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .account(account)
                .tokenHash(tokenHash)
                .expiresAt(now.plusSeconds(jwtProperties.getResetTokenExpirationSeconds()))
                .createdAt(now)
                .build();
        passwordResetTokenRepository.save(passwordResetToken);

        return ResetTokenResponse.builder()
                .resetToken(resetToken)
                .expiresInSeconds(jwtProperties.getResetTokenExpirationSeconds())
                .build();
    }

    @Override
    @Transactional
    public void resetForgotPassword(ForgotPasswordResetRequest request) {
        if (!PasswordUtils.isValid(request.getNewPassword())) {
            throw new AppException(
                    ErrorCode.VALIDATION_FAILED,
                    "Mật khẩu mới phải có từ 8-64 ký tự, gồm chữ hoa, chữ thường và chữ số"
            );
        }

        String phone = PhoneUtils.normalize(request.getPhone());
        String tokenHash = HashUtils.sha256(request.getResetToken());
        OffsetDateTime now = DateTimeUtils.nowVietnam();

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new AppException(ErrorCode.RESET_TOKEN_INVALID));

        if (!resetToken.getAccount().getUsername().equals(phone)) {
            throw new AppException(ErrorCode.RESET_TOKEN_INVALID);
        }

        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(now)) {
            throw new AppException(ErrorCode.RESET_TOKEN_EXPIRED);
        }

        Account account = resetToken.getAccount();
        if (account.getStatus() == AccountStatus.LOCKED) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        account.setFailedLoginCount(0);
        account.setLoginLockedUntil(null);
        account.setTokenVersion(account.getTokenVersion() + 1);
        accountRepository.save(account);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByAccountId(account.getId(), now);

        // Mark reset token as used
        resetToken.setUsedAt(now);
        passwordResetTokenRepository.save(resetToken);
    }

    private UserSummaryResponse buildUserSummary(Account account) {
        if (account.getRole() == Role.CUSTOMER) {
            Customer customer = customerRepository.findByAccountId(account.getId()).orElse(null);
            return UserSummaryResponse.builder()
                    .accountId(account.getId())
                    .fullName(customer != null ? customer.getFullName() : "")
                    .phone(account.getUsername())
                    .role(account.getRole())
                    .avatarUrl(customer != null ? customer.getAvatarUrl() : null)
                    .tier(customer != null ? customer.getCustomerTier() : CustomerTier.BRONZE)
                    .build();
        } else {
            Staff staff = staffRepository.findByAccountId(account.getId()).orElse(null);
            return UserSummaryResponse.builder()
                    .accountId(account.getId())
                    .fullName(staff != null ? staff.getFullName() : "")
                    .phone(account.getUsername())
                    .role(account.getRole())
                    .avatarUrl(staff != null ? staff.getAvatarUrl() : null)
                    .tier(null) // null for staff per specification
                    .build();
        }
    }
}
