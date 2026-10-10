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

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminReportControllerTest {

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
    private String staffToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Account adminAccount = accountRepository.save(Account.builder()
                .username("0955000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build());
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        Account staffAccount = accountRepository.save(Account.builder()
                .username("0955000002")
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
        accountRepository.findByUsername("0955000001").ifPresent(accountRepository::delete);
        accountRepository.findByUsername("0955000002").ifPresent(accountRepository::delete);
    }

    @Test
    @DisplayName("RPT-03: Admin lấy báo cáo doanh thu & lợi nhuận thành công")
    void getRevenueReport_Admin_Success() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        mockMvc.perform(get("/api/v1/admin/reports/revenue")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", sevenDaysAgo.toString())
                        .param("to", today.toString())
                        .param("groupBy", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.summary.netRevenue").isNumber())
                .andExpect(jsonPath("$.data.series").isArray());
    }

    @Test
    @DisplayName("RPT-03: Nhân viên STAFF truy cập báo cáo doanh thu bị 403 Forbidden")
    void getRevenueReport_Staff_Forbidden() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        mockMvc.perform(get("/api/v1/admin/reports/revenue")
                        .header("Authorization", "Bearer " + staffToken)
                        .param("from", sevenDaysAgo.toString())
                        .param("to", today.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RPT-03: Khoảng ngày vượt quá 366 ngày trả về 422 DATE_RANGE_TOO_LARGE")
    void getRevenueReport_DateRangeTooLarge() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports/revenue")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", "2024-01-01")
                        .param("to", "2025-06-01"))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("DATE_RANGE_TOO_LARGE"));
    }

    @Test
    @DisplayName("RPT-04: Lấy danh sách top sách bán chạy thành công")
    void getTopBooksReport_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports/top-books")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("limit", "10")
                        .param("sortBy", "QUANTITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("RPT-05: Lấy báo cáo tồn kho thành công")
    void getInventoryReport_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports/inventory")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary").exists())
                .andExpect(jsonPath("$.data.items").exists());
    }

    @Test
    @DisplayName("RPT-06: Lấy báo cáo phân khúc và tỷ lệ quay lại của khách thành công")
    void getCustomerReport_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports/customers")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tierDistribution").isArray())
                .andExpect(jsonPath("$.data.topCustomers").isArray())
                .andExpect(jsonPath("$.data.repeatPurchaseRate").isNumber());
    }

    @Test
    @DisplayName("RPT-07: Xuất file báo cáo doanh thu CSV thành công")
    void exportRevenueReport_Csv_Success() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        mockMvc.perform(get("/api/v1/admin/reports/revenue/export")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("from", threeDaysAgo.toString())
                        .param("to", today.toString())
                        .param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"bookhub-revenue-")))
                .andExpect(header().string("Content-Type", containsString("text/csv")));
    }
}

