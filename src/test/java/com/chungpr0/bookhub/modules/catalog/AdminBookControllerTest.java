package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateBookRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateBookRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateBookStatusRequest;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminBookControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired(required = false)
    private OrderDetailRepository orderDetailRepository;

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private CartItemRepository cartItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

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
    private Book savedBook;
    private Category category;
    private Author author;
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // Customer account
        Account customerAccount = accountRepository.findByUsername("0911111111").orElseGet(() -> {
            Account acc = Account.builder()
                    .username("0911111111")
                    .passwordHash(passwordEncoder.encode("Password123"))
                    .role(Role.CUSTOMER)
                    .status(AccountStatus.ACTIVE)
                    .tokenVersion(0)
                    .build();
            return accountRepository.save(acc);
        });
        customerToken = jwtTokenProvider.generateAccessToken(customerAccount);

        // Admin account
        Account adminAccount = accountRepository.findByUsername("0922222222").orElseGet(() -> {
            Account acc = Account.builder()
                    .username("0922222222")
                    .passwordHash(passwordEncoder.encode("Password123"))
                    .role(Role.ADMIN)
                    .status(AccountStatus.ACTIVE)
                    .tokenVersion(0)
                    .build();
            return accountRepository.save(acc);
        });
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        category = categoryRepository.save(Category.builder()
                .name("Kinh tế")
                .slug("kinh-te")
                .build());

        author = authorRepository.save(Author.builder()
                .name("Tác giả A")
                .slug("tac-gia-a")
                .build());

        publisher = publisherRepository.save(Publisher.builder()
                .name("NXB Trẻ")
                .slug("nxb-tre")
                .build());

        savedBook = bookRepository.save(Book.builder()
                .title("Kinh tế học cơ bản")
                .slug("kinh-te-hoc-co-ban")
                .category(category)
                .publisher(publisher)
                .authors(List.of(author))
                .originalPrice(120000L)
                .salePrice(100000L)
                .stockQuantity(50)
                .lowStockThreshold(5)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .version(1)
                .images(new ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/books - Thất bại do chưa đăng nhập (401 UNAUTHORIZED)")
    void testGetAdminBooks_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/books - Thất bại do vai trò CUSTOMER không có quyền (403 FORBIDDEN)")
    void testGetAdminBooks_ForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/books - Thành công với vai trò ADMIN (@PreAuthorize hasRole ADMIN) (200 OK)")
    void testGetAdminBooks_SuccessForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].title").value("Kinh tế học cơ bản"))
                .andExpect(jsonPath("$.data.items[0].stockQuantity").value(50))
                .andExpect(jsonPath("$.data.items[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/books/{id} - Thành công lấy chi tiết kỹ thuật cho Admin (200 OK)")
    void testGetAdminBookDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/books/" + savedBook.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Kinh tế học cơ bản"))
                .andExpect(jsonPath("$.data.stockQuantity").value(50))
                .andExpect(jsonPath("$.data.lowStockThreshold").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/admin/books/{id} - Không tìm thấy sách (404 NOT_FOUND)")
    void testGetAdminBookDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/admin/books/999999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/books - 400 BAD_REQUEST khi tiêu đề để trống")
    void testCreateBook_ValidationBlankTitle() throws Exception {
        CreateBookRequest request = CreateBookRequest.builder()
                .title("")
                .build();

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/books - 201 CREATED thêm mới sách thành công")
    void testCreateBook_Success() throws Exception {
        CreateBookRequest request = CreateBookRequest.builder()
                .title("Kinh Tế Vĩ Mô 2026")
                .categoryId(category.getId())
                .publisherId(publisher.getId())
                .authorIds(List.of(author.getId()))
                .originalPrice(150000L)
                .salePrice(120000L)
                .lowStockThreshold(5)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .images(List.of("https://cdn.bookhub.vn/books/macro-econ.webp"))
                .build();

        mockMvc.perform(post("/api/v1/admin/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Kinh Tế Vĩ Mô 2026"))
                .andExpect(jsonPath("$.data.stockQuantity").value(0));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/books/{id} - 409 CONFLICT khi phiên bản (version) không khớp")
    void testUpdateBook_ConcurrentModification() throws Exception {
        UpdateBookRequest request = UpdateBookRequest.builder()
                .title("Kinh tế học cơ bản - Tái bản")
                .categoryId(category.getId())
                .publisherId(publisher.getId())
                .authorIds(List.of(author.getId()))
                .originalPrice(130000L)
                .salePrice(110000L)
                .lowStockThreshold(5)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .version(99) // Stale version
                .images(List.of("https://cdn.bookhub.vn/books/cover.webp"))
                .build();

        mockMvc.perform(put("/api/v1/admin/books/" + savedBook.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CONCURRENT_MODIFICATION"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/books/{id} - 200 OK cập nhật sách thành công khi version khớp")
    void testUpdateBook_Success() throws Exception {
        UpdateBookRequest request = UpdateBookRequest.builder()
                .title("Kinh tế học cơ bản - Bản mới")
                .categoryId(category.getId())
                .publisherId(publisher.getId())
                .authorIds(List.of(author.getId()))
                .originalPrice(140000L)
                .salePrice(115000L)
                .lowStockThreshold(5)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .version(1)
                .images(List.of("https://cdn.bookhub.vn/books/cover2.webp"))
                .build();

        mockMvc.perform(put("/api/v1/admin/books/" + savedBook.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Kinh tế học cơ bản - Bản mới"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/books/{id}/status - 200 OK chuyển trạng thái kinh doanh")
    void testUpdateBookStatus_Success() throws Exception {
        UpdateBookStatusRequest request = UpdateBookStatusRequest.builder()
                .status(BookStatus.INACTIVE)
                .build();

        mockMvc.perform(patch("/api/v1/admin/books/" + savedBook.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/books/{id} - 200 OK xóa sách không có giao dịch thành công")
    void testDeleteBook_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/books/" + savedBook.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        if (orderDetailRepository != null) {
            orderDetailRepository.deleteAll();
        }
        if (orderRepository != null) {
            orderRepository.deleteAll();
        }
        if (cartItemRepository != null) {
            cartItemRepository.deleteAll();
        }
        reviewRepository.deleteAll();
        wishlistRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        authorRepository.deleteAll();
        publisherRepository.deleteAll();
    }
}
