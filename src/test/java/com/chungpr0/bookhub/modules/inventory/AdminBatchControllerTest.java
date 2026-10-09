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
import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import com.chungpr0.bookhub.modules.inventory.entity.StockReceipt;
import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import com.chungpr0.bookhub.modules.inventory.repository.BatchRepository;
import com.chungpr0.bookhub.modules.inventory.repository.InventoryTransactionRepository;
import com.chungpr0.bookhub.modules.inventory.repository.StockReceiptRepository;
import com.chungpr0.bookhub.modules.inventory.repository.SupplierRepository;
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
import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminBatchControllerTest {

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

    private String adminToken;

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

        Supplier supplier = supplierRepository.save(Supplier.builder()
                .name("Công ty Nhã Nam")
                .build());

        Category category = categoryRepository.save(Category.builder().name("Văn học").slug("van-hoc-batch").build());
        Publisher publisher = publisherRepository.save(Publisher.builder().name("NXB Trẻ").slug("nxb-tre-batch").build());

        Book book = bookRepository.save(Book.builder()
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim-batch")
                .isbn("9786045629870")
                .category(category)
                .publisher(publisher)
                .authors(new ArrayList<>())
                .originalPrice(85000L)
                .salePrice(68000L)
                .stockQuantity(100)
                .lowStockThreshold(5)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .images(new ArrayList<>())
                .build());

        StockReceipt receipt = stockReceiptRepository.save(StockReceipt.builder()
                .receiptCode("PN-20261009-BCH1")
                .supplier(supplier)
                .importDate(LocalDate.now().minusDays(5))
                .totalQuantity(100)
                .totalCost(4500000L)
                .createdBy(adminAccount.getId())
                .build());

        batchRepository.save(Batch.builder()
                .receipt(receipt)
                .book(book)
                .batchCode("BATCH-PN-20261009-BCH1-101")
                .importPrice(45000L)
                .quantityImported(100)
                .quantityRemaining(80)
                .importDate(LocalDate.now().minusDays(5))
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/batches - 200 OK lấy danh sách lô hàng theo FIFO")
    void testGetBatches_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/batches")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("hasRemaining", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].batchCode").value("BATCH-PN-20261009-BCH1-101"))
                .andExpect(jsonPath("$.data.items[0].quantityRemaining").value(80));
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

