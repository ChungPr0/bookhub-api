package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.user.dto.request.CreateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRoleRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffStatusRequest;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminStaffControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String staffToken;
    private Account adminAccount;
    private Staff adminStaff;
    private Staff testStaff;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        adminAccount = accountRepository.save(Account.builder()
                .username("0977000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .build());
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        adminStaff = staffRepository.save(Staff.builder()
                .account(adminAccount)
                .fullName("Quản Trị Viên")
                .email("admin@bookhub.vn")
                .build());

        Account staffAccount = accountRepository.save(Account.builder()
                .username("0977000002")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.STAFF)
                .status(AccountStatus.ACTIVE)
                .build());
        staffToken = jwtTokenProvider.generateAccessToken(staffAccount);

        testStaff = staffRepository.save(Staff.builder()
                .account(staffAccount)
                .fullName("Trần Nhân Viên")
                .email("nhanvien@bookhub.vn")
                .createdBy(adminAccount.getId())
                .build());
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        staffRepository.deleteAll();
        accountRepository.findByUsername("0911888999").ifPresent(accountRepository::delete);
        accountRepository.findByUsername("0977000001").ifPresent(accountRepository::delete);
        accountRepository.findByUsername("0977000002").ifPresent(accountRepository::delete);
    }

    @Test
    @DisplayName("AST-01: Admin lấy danh sách nhân viên thành công")
    void getStaffs_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/staffs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("AST-01: Nhân viên STAFF truy cập danh sách nhân viên bị chặn 403 Forbidden")
    void getStaffs_Staff_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/staffs")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AST-02: Lấy chi tiết nhân viên thành công")
    void getStaffDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/staffs/" + testStaff.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Trần Nhân Viên"))
                .andExpect(jsonPath("$.data.role").value("STAFF"));
    }

    @Test
    @DisplayName("AST-03: Tạo tài khoản nhân viên mới trả về 201 Created và mật khẩu tạm")
    void createStaff_Success() throws Exception {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .phone("0911888999")
                .fullName("Lê Văn Khoa")
                .email("khoale@bookhub.vn")
                .role(Role.STAFF)
                .build();

        mockMvc.perform(post("/api/v1/admin/staffs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.temporaryPassword").isString())
                .andExpect(jsonPath("$.data.status").value("UNVERIFIED"));
    }

    @Test
    @DisplayName("AST-03: Trùng số điện thoại khi tạo nhân viên trả về 409 PHONE_ALREADY_REGISTERED")
    void createStaff_PhoneDuplicate() throws Exception {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .phone("0977000002") // trùng với testStaff
                .fullName("Lê Văn Khoa")
                .email("khoale.unique@bookhub.vn")
                .role(Role.STAFF)
                .build();

        mockMvc.perform(post("/api/v1/admin/staffs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PHONE_ALREADY_REGISTERED"));
    }

    @Test
    @DisplayName("AST-04: Cập nhật thông tin nhân viên thành công")
    void updateStaff_Success() throws Exception {
        UpdateStaffRequest request = UpdateStaffRequest.builder()
                .fullName("Trần Nhân Viên Updated")
                .email("nhanvien.updated@bookhub.vn")
                .build();

        mockMvc.perform(put("/api/v1/admin/staffs/" + testStaff.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Trần Nhân Viên Updated"))
                .andExpect(jsonPath("$.data.email").value("nhanvien.updated@bookhub.vn"));
    }

    @Test
    @DisplayName("AST-05: Đổi vai trò nhân sự thành công")
    void updateStaffRole_Success() throws Exception {
        UpdateStaffRoleRequest request = UpdateStaffRoleRequest.builder()
                .role(Role.MANAGER)
                .build();

        mockMvc.perform(patch("/api/v1/admin/staffs/" + testStaff.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("MANAGER"));
    }

    @Test
    @DisplayName("AST-05: Tự đổi vai trò của bản thân bị chặn 403 SELF_MODIFICATION_FORBIDDEN")
    void updateStaffRole_SelfModification_Forbidden() throws Exception {
        UpdateStaffRoleRequest request = UpdateStaffRoleRequest.builder()
                .role(Role.MANAGER)
                .build();

        // adminStaff is linked to adminAccount (the caller)
        mockMvc.perform(patch("/api/v1/admin/staffs/" + adminStaff.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SELF_MODIFICATION_FORBIDDEN"));
    }

    @Test
    @DisplayName("AST-06: Khóa tài khoản nhân viên thành công")
    void updateStaffStatus_Lock_Success() throws Exception {
        UpdateStaffStatusRequest request = UpdateStaffStatusRequest.builder()
                .status(AccountStatus.LOCKED)
                .reason("Nhân viên đã nghỉ việc")
                .build();

        mockMvc.perform(patch("/api/v1/admin/staffs/" + testStaff.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("LOCKED"));
    }

    @Test
    @DisplayName("AST-06: Tự khóa tài khoản của chính mình bị chặn 403 SELF_MODIFICATION_FORBIDDEN")
    void updateStaffStatus_SelfLock_Forbidden() throws Exception {
        UpdateStaffStatusRequest request = UpdateStaffStatusRequest.builder()
                .status(AccountStatus.LOCKED)
                .reason("Tự khóa")
                .build();

        mockMvc.perform(patch("/api/v1/admin/staffs/" + adminStaff.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SELF_MODIFICATION_FORBIDDEN"));
    }

    @Test
    @DisplayName("AST-07: Cấp lại mật khẩu tạm thời thành công")
    void resetStaffPassword_Success() throws Exception {
        mockMvc.perform(post("/api/v1/admin/staffs/" + testStaff.getId() + "/reset-password")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.temporaryPassword").isString())
                .andExpect(jsonPath("$.data.phone").value(testStaff.getAccount().getUsername()));
    }

    @Test
    @DisplayName("AST-07: Tự cấp lại mật khẩu cho chính mình bị chặn 403 SELF_MODIFICATION_FORBIDDEN")
    void resetStaffPassword_SelfReset_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/admin/staffs/" + adminStaff.getId() + "/reset-password")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SELF_MODIFICATION_FORBIDDEN"));
    }
}

