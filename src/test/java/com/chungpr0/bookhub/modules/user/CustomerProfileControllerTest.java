package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateProfileRequest;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CustomerProfileControllerTest {

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
    private StaffRepository staffRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private com.chungpr0.bookhub.modules.cart.repository.CartRepository cartRepository;

    @Autowired
    private com.chungpr0.bookhub.modules.user.repository.AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Account customerAccount;
    private Customer customer;
    private String customerToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        addressRepository.deleteAll();
        pointTransactionRepository.deleteAll();
        cartRepository.deleteAll();
        customerRepository.deleteAll();
        staffRepository.deleteAll();
        accountRepository.deleteAll();

        // Seed Customer
        customerAccount = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .failedLoginCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(customerAccount);

        customer = Customer.builder()
                .account(customerAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .email("chung@gmail.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2002, 4, 3))
                .avatarUrl("https://cdn.bookhub.vn/avatars/avatar-1.webp")
                .rewardPoints(1420)
                .totalSpent(1450000L)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customerRepository.save(customer);

        customerToken = jwtTokenProvider.generateAccessToken(
                customerAccount.getId(),
                customerAccount.getRole(),
                customerAccount.getTokenVersion()
        );
    }

    @Test
    @DisplayName("PRF-01: GET /api/v1/me/profile - Thành công (200 OK)")
    void testGetProfileSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Tiến Chung"))
                .andExpect(jsonPath("$.data.phone").value("0988888888"))
                .andExpect(jsonPath("$.data.tier").value("BRONZE"))
                .andExpect(jsonPath("$.data.tierProgress.nextTier").value("SILVER"))
                .andExpect(jsonPath("$.data.tierProgress.amountToNextTier").value(550000))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("PRF-01: GET /api/v1/me/profile - 401 Unauthorized khi thiếu Token")
    void testGetProfileUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("PRF-01: GET /api/v1/me/profile - 403 Forbidden khi role STAFF gọi API")
    void testGetProfileForbiddenForStaff() throws Exception {
        Account staffAccount = Account.builder()
                .username("0977112233")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.STAFF)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(staffAccount);

        String staffToken = jwtTokenProvider.generateAccessToken(
                staffAccount.getId(),
                staffAccount.getRole(),
                staffAccount.getTokenVersion()
        );

        mockMvc.perform(get("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("PRF-02: PUT /api/v1/me/profile - Thành công (200 OK)")
    void testUpdateProfileSuccess() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyễn Tiến Chung (Đã sửa)")
                .email("chung.updated@gmail.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2002, 4, 3))
                .avatarUrl("https://cdn.bookhub.vn/avatars/new-avatar.webp")
                .build();

        mockMvc.perform(put("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Tiến Chung (Đã sửa)"))
                .andExpect(jsonPath("$.data.email").value("chung.updated@gmail.com"));
    }

    @Test
    @DisplayName("PRF-02: PUT /api/v1/me/profile - 400 Bad Request khi avatarUrl sai domain CDN")
    void testUpdateProfileInvalidAvatarDomain() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyễn Tiến Chung")
                .gender(Gender.MALE)
                .avatarUrl("https://hacker.com/malicious.jpg")
                .build();

        mockMvc.perform(put("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("avatarUrl"));
    }

    @Test
    @DisplayName("PRF-02: PUT /api/v1/me/profile - 400 Bad Request khi birthday ở tương lai")
    void testUpdateProfileFutureBirthday() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyễn Tiến Chung")
                .gender(Gender.MALE)
                .birthday(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(put("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("PRF-02: PUT /api/v1/me/profile - 409 Conflict khi email trùng với người khác")
    void testUpdateProfileDuplicateEmail() throws Exception {
        // Seed another staff with email
        Account staffAcc = Account.builder()
                .username("0911223344")
                .passwordHash("hash")
                .role(Role.STAFF)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(staffAcc);

        Staff staff = Staff.builder()
                .account(staffAcc)
                .fullName("Nhân viên test")
                .email("other@bookhub.vn")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        staffRepository.save(staff);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyễn Tiến Chung")
                .email("other@bookhub.vn")
                .gender(Gender.MALE)
                .build();

        mockMvc.perform(put("/api/v1/me/profile")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_IN_USE"));
    }

    @Test
    @DisplayName("PRF-03: GET /api/v1/me/points - Thành công với phân trang và bộ lọc type (200 OK)")
    void testGetPointsWithFilterAndPagination() throws Exception {
        // Seed 2 point transactions
        PointTransaction tx1 = PointTransaction.builder()
                .customer(customer)
                .type(PointTransactionType.EARN)
                .points(50)
                .balanceAfter(1420)
                .note("Tích điểm từ đơn hàng")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        pointTransactionRepository.save(tx1);

        PointTransaction tx2 = PointTransaction.builder()
                .customer(customer)
                .type(PointTransactionType.REDEEM)
                .points(-20)
                .balanceAfter(1400)
                .note("Dùng điểm")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        pointTransactionRepository.save(tx2);

        // Filter by EARN
        mockMvc.perform(get("/api/v1/me/points?type=EARN&page=0&size=10")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.rewardPoints").value(1420))
                .andExpect(jsonPath("$.data.summary.equivalentValueVnd").value(142000))
                .andExpect(jsonPath("$.data.history.items.length()").value(1))
                .andExpect(jsonPath("$.data.history.items[0].type").value("EARN"));
    }
}
