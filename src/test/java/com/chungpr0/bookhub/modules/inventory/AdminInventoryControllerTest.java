package com.chungpr0.bookhub.modules.inventory;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.inventory.dto.request.AdjustInventoryRequest;
import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentReason;
import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentType;
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
import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminInventoryControllerTest {

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
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private Book testBook;
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Account adminAccount = accountRepository.findByUsername("0922222222").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0922222222")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role(Role.ADMIN)
                        .status(AccountStatus.ACTIVE)
                        .tokenVersion(0)
                        .build()));
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        Category category = categoryRepository.save(Category.builder().name("Văn học").slug("van-hoc-inv").build());
        Publisher publisher = publisherRepository.save(Publisher.builder().name("NXB Trẻ").slug("nxb-tre-inv").build());

        testBook = bookRepository.save(Book.builder()
                .title("Cây Cam Ngọt Của Tôi")
                .slug("cay-cam-ngot-cua-toi-inv")
                .isbn("9786041234567")
                .category(category)
                .publisher(publisher)
                .authors(new ArrayList<>())
                .originalPrice(108000L)
                .salePrice(85000L)
                .stockQuantity(15)
                .lowStockThreshold(20)
                .soldCount(60)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .images(new ArrayList<>())
                .build());

        batchRepository.save(Batch.builder()
                .book(testBook)
                .batchCode("BATCH-ADJ-INIT-108")
                .importPrice(50000L)
                .quantityImported(15)
                .quantityRemaining(15)
                .importDate(LocalDate.now().minusDays(10))
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/admin/inventory/adjustments - 422 UNPROCESSABLE_ENTITY khi điều chỉnh giảm vượt tồn kho")
    void testAdjustInventory_ExceedsStock() throws Exception {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(testBook.getId())
                .type(AdjustmentType.ADJUST_OUT)
                .quantity(20) // current stock is 15
                .reason(AdjustmentReason.LOST)
                .build();

        mockMvc.perform(post("/api/v1/admin/inventory/adjustments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ADJUSTMENT_EXCEEDS_STOCK"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/inventory/adjustments - 200 OK điều chỉnh giảm ADJUST_OUT thành công")
    void testAdjustInventory_AdjustOut_Success() throws Exception {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(testBook.getId())
                .type(AdjustmentType.ADJUST_OUT)
                .quantity(5)
                .reason(AdjustmentReason.DAMAGED)
                .note("Sách bị dính nước")
                .build();

        mockMvc.perform(post("/api/v1/admin/inventory/adjustments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stockBefore").value(15))
                .andExpect(jsonPath("$.data.stockAfter").value(10))
                .andExpect(jsonPath("$.data.affectedBatches", hasSize(1)))
                .andExpect(jsonPath("$.data.affectedBatches[0].quantityDeducted").value(5));

        Book reloaded = bookRepository.findById(testBook.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("POST /api/v1/admin/inventory/adjustments - 200 OK điều chỉnh tăng ADJUST_IN thành công")
    void testAdjustInventory_AdjustIn_Success() throws Exception {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(testBook.getId())
                .type(AdjustmentType.ADJUST_IN)
                .quantity(10)
                .reason(AdjustmentReason.FOUND)
                .importPrice(50000L)
                .build();

        mockMvc.perform(post("/api/v1/admin/inventory/adjustments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stockBefore").value(15))
                .andExpect(jsonPath("$.data.stockAfter").value(25));

        Book reloaded = bookRepository.findById(testBook.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getStockQuantity()).isEqualTo(25);
    }

    @Test
    @DisplayName("GET /api/v1/admin/inventory/transactions - 200 OK tra cứu Thẻ kho kèm summary")
    void testGetTransactions_SuccessWithSummary() throws Exception {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(testBook.getId())
                .type(AdjustmentType.ADJUST_OUT)
                .quantity(2)
                .reason(AdjustmentReason.DAMAGED)
                .note("Hỏng nhẹ")
                .build();

        mockMvc.perform(post("/api/v1/admin/inventory/adjustments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/inventory/transactions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("bookId", String.valueOf(testBook.getId()))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.summary.bookTitle").value("Cây Cam Ngọt Của Tôi"))
                .andExpect(jsonPath("$.data.summary.totalAdjusted").value(-2));
    }

    @Test
    @DisplayName("GET /api/v1/admin/inventory/low-stock - 200 OK cảnh báo sách sắp hết hàng")
    void testGetLowStockBooks_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/inventory/low-stock")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.lowStockCount").value(1))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].stockQuantity").value(15))
                .andExpect(jsonPath("$.data.items[0].stockStatus").value("LOW_STOCK"));
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
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();
    }
}

