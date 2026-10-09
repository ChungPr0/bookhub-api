package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateAuthorRequest;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminAuthorControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

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
    private Author authorWithBooks;
    private Author standaloneAuthor;

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

        authorWithBooks = authorRepository.save(Author.builder()
                .name("Paulo Coelho")
                .slug("paulo-coelho")
                .biography("Tác giả cuốn Nhà Giả Kim")
                .build());

        standaloneAuthor = authorRepository.save(Author.builder()
                .name("Nguyễn Nhật Ánh")
                .slug("nguyen-nhat-anh")
                .biography("Nhà văn tuổi thơ")
                .build());

        Category category = categoryRepository.save(Category.builder().name("Văn học").slug("van-hoc").build());
        Publisher publisher = publisherRepository.save(Publisher.builder().name("NXB Hội Nhà Văn").slug("nxb-hoi-nha-van").build());

        bookRepository.save(Book.builder()
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .category(category)
                .publisher(publisher)
                .authors(List.of(authorWithBooks))
                .originalPrice(79000L)
                .salePrice(63200L)
                .stockQuantity(10)
                .lowStockThreshold(2)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .images(new ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/authors - 401 UNAUTHORIZED khi chưa đăng nhập")
    void testGetAdminAuthors_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/authors"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/authors - 403 FORBIDDEN khi người dùng là CUSTOMER")
    void testGetAdminAuthors_ForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/authors")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/authors - 200 OK tìm kiếm và phân trang cho ADMIN")
    void testGetAdminAuthors_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/authors")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "Paulo")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].name").value("Paulo Coelho"))
                .andExpect(jsonPath("$.data.items[0].bookCount").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/admin/authors/{id} - 200 OK lấy chi tiết hồ sơ tác giả")
    void testGetAdminAuthorDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/authors/" + authorWithBooks.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Paulo Coelho"))
                .andExpect(jsonPath("$.data.biography").value("Tác giả cuốn Nhà Giả Kim"))
                .andExpect(jsonPath("$.data.bookCount").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/admin/authors - 400 BAD_REQUEST khi tên để trống")
    void testCreateAuthor_ValidationBlankName() throws Exception {
        CreateAuthorRequest request = CreateAuthorRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/admin/authors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/authors - 201 CREATED thêm tác giả mới thành công")
    void testCreateAuthor_Success() throws Exception {
        CreateAuthorRequest request = CreateAuthorRequest.builder()
                .name("Haruki Murakami")
                .biography("Tác giả Rừng Na Uy")
                .build();

        mockMvc.perform(post("/api/v1/admin/authors")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Haruki Murakami"))
                .andExpect(jsonPath("$.data.slug").value("haruki-murakami"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/authors/{id} - 200 OK cập nhật tác giả thành công")
    void testUpdateAuthor_Success() throws Exception {
        UpdateAuthorRequest request = UpdateAuthorRequest.builder()
                .name("Nguyễn Nhật Ánh (Nhà văn)")
                .biography("Tiểu sử cập nhật")
                .build();

        mockMvc.perform(put("/api/v1/admin/authors/" + standaloneAuthor.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Nguyễn Nhật Ánh (Nhà văn)"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/authors/{id} - 409 CONFLICT khi tác giả còn sách")
    void testDeleteAuthor_HasBooks() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/authors/" + authorWithBooks.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHOR_HAS_BOOKS"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/authors/{id} - 200 OK xóa tác giả khi không có sách")
    void testDeleteAuthor_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/authors/" + standaloneAuthor.getId())
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
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();
    }
}
