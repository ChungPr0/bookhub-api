package com.chungpr0.bookhub.modules.inventory;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.UpdateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.entity.StockReceipt;
import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import com.chungpr0.bookhub.modules.inventory.repository.BatchRepository;
import com.chungpr0.bookhub.modules.inventory.repository.InventoryTransactionRepository;
import com.chungpr0.bookhub.modules.inventory.repository.StockReceiptRepository;
import com.chungpr0.bookhub.modules.inventory.repository.SupplierRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminSupplierControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private StockReceiptRepository stockReceiptRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerToken;
    private String adminToken;
    private Supplier supplierWithReceipts;
    private Supplier standaloneSupplier;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Account customerAccount = accountRepository.findByUsername("0911111111").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0911111111")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role(Role.CUSTOMER)
                        .status(AccountStatus.ACTIVE)
                        .tokenVersion(0)
                        .build()));
        customerToken = jwtTokenProvider.generateAccessToken(customerAccount);

        Account adminAccount = accountRepository.findByUsername("0922222222").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0922222222")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role(Role.ADMIN)
                        .status(AccountStatus.ACTIVE)
                        .tokenVersion(0)
                        .build()));
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        supplierWithReceipts = supplierRepository.save(Supplier.builder()
                .name("Công ty Nhã Nam")
                .contactName("Trần Văn Nam")
                .phone("02435146875")
                .email("kinhdoanh@nhanam.vn")
                .address("59 Đỗ Quang, Hà Nội")
                .taxCode("0101824123")
                .build());

        standaloneSupplier = supplierRepository.save(Supplier.builder()
                .name("Alpha Books")
                .contactName("Nguyễn Hoàng Anh")
                .phone("02437226234")
                .email("contact@alphabooks.vn")
                .address("11A Thể Giao, Hà Nội")
                .taxCode("0101678999")
                .build());

        stockReceiptRepository.save(StockReceipt.builder()
                .receiptCode("PN-20261009-SUPP")
                .supplier(supplierWithReceipts)
                .importDate(LocalDate.now())
                .totalQuantity(100)
                .totalCost(5000000L)
                .createdBy(adminAccount.getId())
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/suppliers - 401 UNAUTHORIZED khi chưa đăng nhập")
    void testGetSuppliers_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/suppliers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/suppliers - 403 FORBIDDEN khi người dùng là CUSTOMER")
    void testGetSuppliers_ForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/suppliers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/suppliers - 200 OK tìm kiếm và phân trang cho ADMIN")
    void testGetSuppliers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "Nhã Nam")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].name").value("Công ty Nhã Nam"))
                .andExpect(jsonPath("$.data.items[0].receiptCount").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/admin/suppliers/{id} - 200 OK lấy chi tiết nhà cung cấp")
    void testGetSupplierDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/suppliers/" + supplierWithReceipts.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Công ty Nhã Nam"))
                .andExpect(jsonPath("$.data.receiptCount").value(1))
                .andExpect(jsonPath("$.data.totalImportCost").value(5000000));
    }

    @Test
    @DisplayName("GET /api/v1/admin/suppliers/{id} - 404 NOT_FOUND khi ID không tồn tại")
    void testGetSupplierDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/suppliers/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("SUPPLIER_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/suppliers - 400 BAD_REQUEST khi tên để trống")
    void testCreateSupplier_ValidationBlankName() throws Exception {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/admin/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/suppliers - 409 CONFLICT khi tên nhà cung cấp bị trùng")
    void testCreateSupplier_DuplicateName() throws Exception {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .name("Công ty Nhã Nam")
                .build();

        mockMvc.perform(post("/api/v1/admin/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("SUPPLIER_NAME_DUPLICATE"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/suppliers - 201 CREATED thêm nhà cung cấp mới thành công")
    void testCreateSupplier_Success() throws Exception {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .name("Fahasa")
                .contactName("Lê Văn C")
                .phone("02838225446")
                .email("info@fahasa.com")
                .address("60-62 Lê Lợi, Q.1, TP.HCM")
                .taxCode("0301458999")
                .build();

        mockMvc.perform(post("/api/v1/admin/suppliers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Fahasa"))
                .andExpect(jsonPath("$.data.receiptCount").value(0));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/suppliers/{id} - 200 OK cập nhật thông tin nhà cung cấp")
    void testUpdateSupplier_Success() throws Exception {
        UpdateSupplierRequest request = UpdateSupplierRequest.builder()
                .name("Alpha Books (Sài Gòn)")
                .contactName("Nguyễn Hoàng Anh")
                .phone("02437226234")
                .email("contact@alphabooks.vn")
                .address("Quận 1, TP.HCM")
                .taxCode("0101678999")
                .build();

        mockMvc.perform(put("/api/v1/admin/suppliers/" + standaloneSupplier.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Alpha Books (Sài Gòn)"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/suppliers/{id} - 409 CONFLICT khi nhà cung cấp đã có phiếu nhập")
    void testDeleteSupplier_HasReceipts() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/suppliers/" + supplierWithReceipts.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("SUPPLIER_HAS_RECEIPTS"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/suppliers/{id} - 200 OK xóa nhà cung cấp khi chưa có phiếu nhập")
    void testDeleteSupplier_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/suppliers/" + standaloneSupplier.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        inventoryTransactionRepository.deleteAll();
        batchRepository.deleteAll();
        stockReceiptRepository.deleteAll();
        supplierRepository.deleteAll();
    }
}

