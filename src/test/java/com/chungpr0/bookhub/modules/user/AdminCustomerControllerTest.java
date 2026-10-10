package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.user.dto.request.AdjustPointsRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerStatusRequest;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminCustomerControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String staffToken;
    private String customerToken;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Account adminAccount = accountRepository.save(Account.builder()
                .username("0988000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build());
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        Account staffAccount = accountRepository.save(Account.builder()
                .username("0988000002")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.STAFF)
                .status(AccountStatus.ACTIVE)
                .build());
        staffToken = jwtTokenProvider.generateAccessToken(staffAccount);

        Account custAccount = accountRepository.save(Account.builder()
                .username("0988000003")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .build());
        customerToken = jwtTokenProvider.generateAccessToken(custAccount);

        testCustomer = customerRepository.save(Customer.builder()
                .account(custAccount)
                .fullName("Nguyễn Khách Hàng")
                .phone("0988000003")
                .email("cust@example.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(1998, 8, 8))
                .rewardPoints(500)
                .totalSpent(1500000L)
                .customerTier(CustomerTier.BRONZE)
                .build());
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        pointTransactionRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.findByUsername("0988000001").ifPresent(accountRepository::delete);
        accountRepository.findByUsername("0988000002").ifPresent(accountRepository::delete);
        accountRepository.findByUsername("0988000003").ifPresent(accountRepository::delete);
    }

    @Test
    @DisplayName("ACU-01: Admin lấy danh sách khách hàng thành công kèm phân trang PageResponse")
    void getCustomers_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/customers")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.data.page.size").value(10))
                .andExpect(jsonPath("$.data.page.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("ACU-01: Khách hàng thường truy cập danh sách khách hàng bị 403 Forbidden")
    void getCustomers_CustomerRole_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/customers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ACU-02: Lấy chi tiết khách hàng thành công")
    void getCustomerDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/customers/" + testCustomer.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Khách Hàng"))
                .andExpect(jsonPath("$.data.tierProgress").exists())
                .andExpect(jsonPath("$.data.orderStats").exists());
    }

    @Test
    @DisplayName("ACU-02: Lấy chi tiết khách hàng không tồn tại trả về 404 CUSTOMER_NOT_FOUND")
    void getCustomerDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/customers/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    @DisplayName("ACU-03: Cập nhật thông tin khách hàng thành công bởi Admin")
    void updateCustomer_Admin_Success() throws Exception {
        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .fullName("Nguyễn Khách Hàng Updated")
                .email("cust.updated@example.com")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.of(1999, 9, 9))
                .build();

        mockMvc.perform(put("/api/v1/admin/customers/" + testCustomer.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Nguyễn Khách Hàng Updated"))
                .andExpect(jsonPath("$.data.email").value("cust.updated@example.com"));
    }

    @Test
    @DisplayName("ACU-03: Nhân viên STAFF sửa thông tin khách hàng bị từ chối 403 Forbidden")
    void updateCustomer_Staff_Forbidden() throws Exception {
        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .fullName("Nguyễn Khách Hàng Updated")
                .email("cust.updated@example.com")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.of(1999, 9, 9))
                .build();

        mockMvc.perform(put("/api/v1/admin/customers/" + testCustomer.getId())
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ACU-04: Khóa tài khoản khách hàng thành công")
    void updateCustomerStatus_Lock_Success() throws Exception {
        UpdateCustomerStatusRequest request = UpdateCustomerStatusRequest.builder()
                .status(AccountStatus.LOCKED)
                .reason("Phát hiện đặt hàng ảo không nhận liên tiếp")
                .build();

        mockMvc.perform(patch("/api/v1/admin/customers/" + testCustomer.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("LOCKED"))
                .andExpect(jsonPath("$.data.reason").value("Phát hiện đặt hàng ảo không nhận liên tiếp"));
    }

    @Test
    @DisplayName("ACU-05: Xem lịch sử đơn hàng của khách hàng")
    void getCustomerOrders_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/customers/" + testCustomer.getId() + "/orders")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("ACU-06: Điều chỉnh điểm thưởng khách hàng thành công")
    void adjustPoints_Success() throws Exception {
        AdjustPointsRequest request = AdjustPointsRequest.builder()
                .points(300)
                .reason("Tặng điểm bù sự cố giao hàng")
                .build();

        mockMvc.perform(post("/api/v1/admin/customers/" + testCustomer.getId() + "/points/adjustments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.points").value(300))
                .andExpect(jsonPath("$.data.balanceAfter").value(800)); // 500 + 300
    }

    @Test
    @DisplayName("ACU-06: Trừ điểm vượt quá số dư hiện có trả về 422 INSUFFICIENT_POINTS")
    void adjustPoints_InsufficientPoints() throws Exception {
        AdjustPointsRequest request = AdjustPointsRequest.builder()
                .points(-1000)
                .reason("Phạt trừ điểm")
                .build();

        mockMvc.perform(post("/api/v1/admin/customers/" + testCustomer.getId() + "/points/adjustments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_POINTS"));
    }

    @Test
    @DisplayName("ACU-07: Xem lịch sử biến động điểm của khách hàng thành công")
    void getCustomerPointsHistory_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/customers/" + testCustomer.getId() + "/points/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }
}

