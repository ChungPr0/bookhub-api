package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreatePublisherRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdatePublisherRequest;
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
class AdminPublisherControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private PublisherRepository publisherRepository;

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
    private Publisher publisherWithBooks;
    private Publisher standalonePublisher;

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

        publisherWithBooks = publisherRepository.save(Publisher.builder()
                .name("NXB Trẻ")
                .slug("nxb-tre")
                .address("161B Lý Chính Thắng, Q.3, TP.HCM")
                .website("https://www.nxbtre.com.vn")
                .build());

        standalonePublisher = publisherRepository.save(Publisher.builder()
                .name("NXB Phụ Nữ")
                .slug("nxb-phu-nu")
                .address("39 Hàng Chuối, Hà Nội")
                .website("https://nxbphunu.com.vn")
                .build());

        Category category = categoryRepository.save(Category.builder().name("Văn học").slug("van-hoc").build());
        Author author = authorRepository.save(Author.builder().name("Nguyễn Nhật Ánh").slug("nguyen-nhat-anh").build());

        bookRepository.save(Book.builder()
                .title("Cho Tôi Xin Một Vé Đi Tuổi Thơ")
                .slug("cho-toi-xin-mot-ve-di-tuoi-tho")
                .category(category)
                .publisher(publisherWithBooks)
                .authors(List.of(author))
                .originalPrice(85000L)
                .salePrice(68000L)
                .stockQuantity(15)
                .lowStockThreshold(3)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .images(new ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/publishers - 401 UNAUTHORIZED khi chưa đăng nhập")
    void testGetAdminPublishers_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/publishers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/publishers - 403 FORBIDDEN khi người dùng là CUSTOMER")
    void testGetAdminPublishers_ForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/publishers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/publishers - 200 OK tìm kiếm và phân trang cho ADMIN")
    void testGetAdminPublishers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/publishers")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "Trẻ")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].name").value("NXB Trẻ"))
                .andExpect(jsonPath("$.data.items[0].bookCount").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/admin/publishers/{id} - 200 OK lấy chi tiết nhà xuất bản")
    void testGetAdminPublisherDetail_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/publishers/" + publisherWithBooks.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("NXB Trẻ"))
                .andExpect(jsonPath("$.data.address").value("161B Lý Chính Thắng, Q.3, TP.HCM"))
                .andExpect(jsonPath("$.data.bookCount").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/admin/publishers - 400 BAD_REQUEST khi tên để trống")
    void testCreatePublisher_ValidationBlankName() throws Exception {
        CreatePublisherRequest request = CreatePublisherRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/admin/publishers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/publishers - 409 CONFLICT khi tên nhà xuất bản bị trùng")
    void testCreatePublisher_DuplicateName() throws Exception {
        CreatePublisherRequest request = CreatePublisherRequest.builder()
                .name("NXB Trẻ")
                .build();

        mockMvc.perform(post("/api/v1/admin/publishers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("PUBLISHER_NAME_DUPLICATE"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/publishers - 201 CREATED thêm nhà xuất bản mới thành công")
    void testCreatePublisher_Success() throws Exception {
        CreatePublisherRequest request = CreatePublisherRequest.builder()
                .name("NXB Kim Đồng")
                .address("55 Quang Trung, Hà Nội")
                .website("https://nxbkimdong.com.vn")
                .build();

        mockMvc.perform(post("/api/v1/admin/publishers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("NXB Kim Đồng"))
                .andExpect(jsonPath("$.data.slug").value("nxb-kim-dong"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/publishers/{id} - 200 OK cập nhật nhà xuất bản thành công")
    void testUpdatePublisher_Success() throws Exception {
        UpdatePublisherRequest request = UpdatePublisherRequest.builder()
                .name("NXB Phụ Nữ Việt Nam")
                .address("39 Hàng Chuối, Hai Bà Trưng, Hà Nội")
                .website("https://nxbphunuvn.com.vn")
                .build();

        mockMvc.perform(put("/api/v1/admin/publishers/" + standalonePublisher.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("NXB Phụ Nữ Việt Nam"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/publishers/{id} - 409 CONFLICT khi nhà xuất bản còn sách")
    void testDeletePublisher_HasBooks() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/publishers/" + publisherWithBooks.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("PUBLISHER_HAS_BOOKS"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/publishers/{id} - 200 OK xóa nhà xuất bản khi không có sách")
    void testDeletePublisher_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/publishers/" + standalonePublisher.getId())
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
        publisherRepository.deleteAll();
        categoryRepository.deleteAll();
        authorRepository.deleteAll();
    }
}
