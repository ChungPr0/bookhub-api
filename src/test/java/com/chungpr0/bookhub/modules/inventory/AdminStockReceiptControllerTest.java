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
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateStockReceiptRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.StockReceiptItemRequest;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminStockReceiptControllerTest {

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

    private String customerToken;
    private String adminToken;
    private Supplier testSupplier;
    private Book testBook;

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

        testSupplier = supplierRepository.save(Supplier.builder()
                .name("Công ty Nhã Nam")
                .contactName("Trần Văn Nam")
                .phone("02435146875")
                .email("kinhdoanh@nhanam.vn")
                .build());

        Category category = categoryRepository.save(Category.builder().name("Văn học").slug("van-hoc-test").build());
        Publisher publisher = publisherRepository.save(Publisher.builder().name("NXB Trẻ").slug("nxb-tre-test").build());

        testBook = bookRepository.save(Book.builder()
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim-stk")
                .isbn("9786045629870")
                .category(category)
                .publisher(publisher)
                .authors(new ArrayList<>())
                .originalPrice(85000L)
                .salePrice(68000L)
                .stockQuantity(10)
                .lowStockThreshold(5)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .images(new ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/admin/stock-receipts - 401 UNAUTHORIZED khi chưa đăng nhập")
    void testCreateStockReceipt_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/admin/stock-receipts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/stock-receipts - 403 FORBIDDEN khi người dùng là CUSTOMER")
    void testCreateStockReceipt_ForbiddenForCustomer() throws Exception {
        mockMvc.perform(post("/api/v1/admin/stock-receipts")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/stock-receipts - 400 BAD_REQUEST khi danh sách items rỗng")
    void testCreateStockReceipt_EmptyItems() throws Exception {
        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(testSupplier.getId())
                .importDate(LocalDate.now())
                .items(List.of())
                .build();

        mockMvc.perform(post("/api/v1/admin/stock-receipts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/stock-receipts - 201 CREATED tạo phiếu nhập kho thành công")
    void testCreateStockReceipt_Success() throws Exception {
        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(testSupplier.getId())
                .importDate(LocalDate.now())
                .note("Nhập sách đợt tháng 10")
                .items(List.of(
                        StockReceiptItemRequest.builder()
                                .bookId(testBook.getId())
                                .quantity(100)
                                .importPrice(45000L)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/admin/stock-receipts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receipt.totalQuantity").value(100))
                .andExpect(jsonPath("$.data.receipt.totalCost").value(4500000))
                .andExpect(jsonPath("$.data.receipt.items", hasSize(1)))
                .andExpect(jsonPath("$.data.receipt.items[0].quantityImported").value(100));

        Book reloaded = bookRepository.findById(testBook.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getStockQuantity()).isEqualTo(110);
    }

    @Test
    @DisplayName("GET /api/v1/admin/stock-receipts - 200 OK lấy danh sách phân trang phiếu nhập")
    void testGetStockReceipts_Success() throws Exception {
        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(testSupplier.getId())
                .importDate(LocalDate.now())
                .items(List.of(
                        StockReceiptItemRequest.builder()
                                .bookId(testBook.getId())
                                .quantity(50)
                                .importPrice(45000L)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/admin/stock-receipts")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/admin/stock-receipts")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/admin/stock-receipts/{id} - 404 NOT_FOUND khi ID không tồn tại")
    void testGetStockReceiptDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stock-receipts/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("STOCK_RECEIPT_NOT_FOUND"));
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

