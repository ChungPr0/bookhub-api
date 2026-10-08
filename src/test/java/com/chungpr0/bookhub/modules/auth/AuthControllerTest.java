package com.chungpr0.bookhub.modules.auth;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.OtpPurpose;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.common.util.HashUtils;
import com.chungpr0.bookhub.modules.auth.dto.request.ChangePasswordRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordResetRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.ForgotPasswordVerifyRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.LoginRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.LogoutRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RefreshTokenRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterOtpRequest;
import com.chungpr0.bookhub.modules.auth.dto.request.RegisterRequest;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.entity.Otp;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.auth.repository.OtpRepository;
import com.chungpr0.bookhub.modules.auth.repository.PasswordResetTokenRepository;
import com.chungpr0.bookhub.modules.auth.repository.RefreshTokenRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        passwordResetTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        cartRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();
        otpRepository.deleteAll();
    }

    @Test
    @DisplayName("AUTH-01: Yêu cầu gửi OTP đăng ký thành công")
    void testRequestRegisterOtpSuccess() throws Exception {
        RegisterOtpRequest request = RegisterOtpRequest.builder()
                .phone("0988888888")
                .build();

        mockMvc.perform(post("/api/v1/auth/register/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.phone").value("098****888"))
                .andExpect(jsonPath("$.data.otpExpiresInSeconds").value(300))
                .andExpect(jsonPath("$.data.resendAfterSeconds").value(60));

        assertThat(otpRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("AUTH-01: Thất bại khi số điện thoại sai định dạng")
    void testRequestRegisterOtpInvalidPhone() throws Exception {
        RegisterOtpRequest request = RegisterOtpRequest.builder()
                .phone("12345")
                .build();

        mockMvc.perform(post("/api/v1/auth/register/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("phone"));
    }

    @Test
    @DisplayName("AUTH-01: Thất bại khi gửi lại OTP quá sớm (<60s)")
    void testRequestRegisterOtpTooFrequent() throws Exception {
        RegisterOtpRequest request = RegisterOtpRequest.builder()
                .phone("0988888888")
                .build();

        // Lần 1 thành công
        mockMvc.perform(post("/api/v1/auth/register/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Lần 2 ngay lập tức -> 429
        mockMvc.perform(post("/api/v1/auth/register/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("OTP_REQUEST_TOO_FREQUENT"))
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    @DisplayName("AUTH-02: Đăng ký tài khoản thành công kèm giỏ hàng")
    void testRegisterSuccess() throws Exception {
        String phone = "0988888888";
        String otpCode = "123456";

        // Seed OTP
        Otp otp = Otp.builder()
                .phone(phone)
                .purpose(OtpPurpose.REGISTER)
                .otpHash(HashUtils.sha256(phone + ":" + otpCode + ":REGISTER"))
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .createdAt(OffsetDateTime.now())
                .build();
        otpRepository.save(otp);

        RegisterRequest request = RegisterRequest.builder()
                .phone(phone)
                .otpCode(otpCode)
                .fullName("Nguyễn Tiến Chung")
                .password("Password123!")
                .email("chung@gmail.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2002, 4, 3))
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.phone").value(phone))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Tiến Chung"));

        assertThat(accountRepository.existsByUsername(phone)).isTrue();
        assertThat(customerRepository.findByPhone(phone)).isPresent();
        assertThat(cartRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("AUTH-03 & AUTH-04: Đăng nhập và Refresh Token Rotation")
    void testLoginAndRefreshTokenRotation() throws Exception {
        String phone = "0988888888";
        String password = "Password123!";

        // Seed Account & Customer
        Account account = Account.builder()
                .username(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(account);

        LoginRequest loginRequest = LoginRequest.builder()
                .phone(phone)
                .password(password)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("data").get("accessToken").asText();
        String refreshToken1 = loginJson.get("data").get("refreshToken").asText();

        // Test AUTH-07: Lấy thông tin cá nhân /me
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value(phone))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));

        // Test AUTH-04: Cấp mới token (Refresh Token Rotation)
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(refreshToken1)
                .build();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String refreshToken2 = refreshJson.get("data").get("refreshToken").asText();
        assertThat(refreshToken2).isNotEqualTo(refreshToken1);

        // Test Token Reuse Detection: Dùng lại refreshToken1 đã thu hồi -> 401 REFRESH_TOKEN_REUSED
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REUSED"));
    }

    @Test
    @DisplayName("AUTH-08: Đổi mật khẩu chủ động")
    void testChangePassword() throws Exception {
        String phone = "0988888888";
        String oldPassword = "Password123!";
        String newPassword = "NewPassword456!";

        Account account = Account.builder()
                .username(phone)
                .passwordHash(passwordEncoder.encode(oldPassword))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(account);

        // Login first
        LoginRequest loginRequest = LoginRequest.builder().phone(phone).password(oldPassword).build();
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        // Change password
        ChangePasswordRequest changeRequest = ChangePasswordRequest.builder()
                .currentPassword(oldPassword)
                .newPassword(newPassword)
                .build();

        mockMvc.perform(put("/api/v1/auth/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // Verify login with new password
        LoginRequest newLoginRequest = LoginRequest.builder().phone(phone).password(newPassword).build();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AUTH-09, AUTH-10, AUTH-11: Toàn bộ luồng quên mật khẩu")
    void testForgotPasswordFlow() throws Exception {
        String phone = "0988888888";
        String oldPassword = "Password123!";
        String newPassword = "ResetPassword789!";
        String otpCode = "654321";

        Account account = Account.builder()
                .username(phone)
                .passwordHash(passwordEncoder.encode(oldPassword))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(account);

        // Step 1: Request OTP
        ForgotPasswordOtpRequest otpRequest = ForgotPasswordOtpRequest.builder().phone(phone).build();
        mockMvc.perform(post("/api/v1/auth/password/forgot/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // Seed precise OTP for Step 2
        Otp otp = Otp.builder()
                .phone(phone)
                .purpose(OtpPurpose.RESET_PASSWORD)
                .otpHash(HashUtils.sha256(phone + ":" + otpCode + ":RESET_PASSWORD"))
                .expiresAt(OffsetDateTime.now().plusMinutes(5))
                .createdAt(OffsetDateTime.now())
                .build();
        otpRepository.save(otp);

        // Step 2: Verify OTP -> Receive resetToken
        ForgotPasswordVerifyRequest verifyRequest = ForgotPasswordVerifyRequest.builder()
                .phone(phone)
                .otpCode(otpCode)
                .build();

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/password/forgot/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetToken").isNotEmpty())
                .andReturn();

        String resetToken = objectMapper.readTree(verifyResult.getResponse().getContentAsString())
                .get("data").get("resetToken").asText();

        // Step 3: Reset password
        ForgotPasswordResetRequest resetRequest = ForgotPasswordResetRequest.builder()
                .phone(phone)
                .resetToken(resetToken)
                .newPassword(newPassword)
                .build();

        mockMvc.perform(post("/api/v1/auth/password/forgot/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // Step 4: Verify can login with new password
        LoginRequest loginRequest = LoginRequest.builder().phone(phone).password(newPassword).build();
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AUTH-05 & AUTH-06: Đăng xuất phiên hiện tại và đăng xuất toàn bộ thiết bị")
    void testLogoutAndLogoutAll() throws Exception {
        String phone = "0988888888";
        String password = "Password123!";

        Account account = Account.builder()
                .username(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(account);

        LoginRequest loginRequest = LoginRequest.builder().phone(phone).password(password).build();
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginJson.get("data").get("accessToken").asText();
        String refreshToken = loginJson.get("data").get("refreshToken").asText();

        // AUTH-05: Logout phiên hiện tại
        LogoutRequest logoutRequest = LogoutRequest.builder().refreshToken(refreshToken).build();
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // Refresh với token vừa bị thu hồi -> 401 REFRESH_TOKEN_REUSED
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder().refreshToken(refreshToken).build();
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_REUSED"));

        // Login lại để lấy token mới test logout-all
        MvcResult loginResult2 = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken2 = objectMapper.readTree(loginResult2.getResponse().getContentAsString())
                .get("data").get("accessToken").asText();

        // AUTH-06: Logout-all
        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        // Sau khi logout-all, access token cũ bị từ chối với 401 TOKEN_REVOKED
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken2))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_REVOKED"));
    }

    @Test
    @DisplayName("AUTH-03: Khóa tài khoản tạm thời 15 phút khi nhập sai mật khẩu 5 lần")
    void testAccountTemporaryLockoutAfter5FailedAttempts() throws Exception {
        String phone = "0988888888";
        String password = "Password123!";

        Account account = Account.builder()
                .username(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(account);

        LoginRequest wrongLogin = LoginRequest.builder().phone(phone).password("WrongPassword1!").build();

        // 4 lần đầu -> 401 INVALID_CREDENTIALS
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(wrongLogin)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.details.remainingAttempts").value(5 - i));
        }

        // Lần thứ 5 -> 423 ACCOUNT_TEMPORARILY_LOCKED
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_TEMPORARILY_LOCKED"))
                .andExpect(header().exists("Retry-After"));
    }
}
