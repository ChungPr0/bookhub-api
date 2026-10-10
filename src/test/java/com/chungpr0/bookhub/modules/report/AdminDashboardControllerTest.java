package com.chungpr0.bookhub.modules.report;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminDashboardControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String managerToken;
    private String staffToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Account adminAccount = accountRepository.save(Account.builder()
                .username("0966000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build());
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        Account managerAccount = accountRepository.save(Account.builder()
                .username("0966000002")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.MANAGER)
                .status(AccountStatus.ACTIVE)
                .build());
        managerToken = jwtTokenProvider.generateAccessToken(managerAccount);

        Account staffAccount = accountRepository.save(Account.builder()
                .username("0966000003")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.STAFF)
                .status(AccountStatus.ACTIVE)
                .build());
        staffToken = jwtTokenProvider.generateAccessToken(staffAccount);
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        accountRepository.findByUsername("0966000001").ifPresent(accountRepository::delete);
        accountRepository.findByUsername("0966000002").ifPresent(accountRepository::delete);
        accountRepository.findByUsername("0966000003").ifPresent(accountRepository::delete);
    }

    @Test
    @DisplayName("RPT-01: Admin xem dashboard tổng quan thành công")
    void getDashboardSummary_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("period", "TODAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.period.type").value("TODAY"))
                .andExpect(jsonPath("$.data.revenue").exists())
                .andExpect(jsonPath("$.data.orders").exists())
                .andExpect(jsonPath("$.data.newCustomers").exists())
                .andExpect(jsonPath("$.data.pendingActions").exists())
                .andExpect(jsonPath("$.data.revenueChart").isArray())
                .andExpect(jsonPath("$.data.topBooks").isArray());
    }

    @Test
    @DisplayName("RPT-01: Manager xem dashboard tổng quan thành công")
    void getDashboardSummary_Manager_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .header("Authorization", "Bearer " + managerToken)
                        .param("period", "LAST_7_DAYS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.period.type").value("LAST_7_DAYS"));
    }

    @Test
    @DisplayName("RPT-01: Nhân viên STAFF truy cập dashboard bị 403 Forbidden")
    void getDashboardSummary_Staff_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RPT-02: Admin xem số liệu vận hành thời gian thực thành công")
    void getDashboardRealtime_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/realtime")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.asOfTime").exists())
                .andExpect(jsonPath("$.data.todaySummary").exists())
                .andExpect(jsonPath("$.data.hourlyBreakdown").isArray())
                .andExpect(jsonPath("$.data.urgentQueues").exists());
    }

    @Test
    @DisplayName("RPT-02: Nhân viên STAFF truy cập realtime bị 403 Forbidden")
    void getDashboardRealtime_Staff_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/realtime")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }
}

