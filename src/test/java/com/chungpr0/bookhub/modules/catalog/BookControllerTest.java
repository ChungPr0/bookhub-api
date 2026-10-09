package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.entity.Review;
import com.chungpr0.bookhub.modules.catalog.entity.Wishlist;
import com.chungpr0.bookhub.modules.catalog.entity.WishlistId;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BookControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

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
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired(required = false)
    private OrderDetailRepository orderDetailRepository;

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private CartItemRepository cartItemRepository;

    private Book savedBook;
    private Category savedCategory;
    private Author savedAuthor;
    private Publisher savedPublisher;
    private Customer savedCustomer;
    private String customerToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        savedCategory = Category.builder()
                .name("Văn học")
                .slug("van-hoc")
                .sortOrder(0)
                .build();
        categoryRepository.save(savedCategory);

        savedAuthor = Author.builder()
                .name("Paulo Coelho")
                .slug("paulo-coelho")
                .biography("Tác giả nổi tiếng")
                .build();
        authorRepository.save(savedAuthor);

        savedPublisher = Publisher.builder()
                .name("NXB Hội Nhà Văn")
                .slug("nxb-hoi-nha-van")
                .build();
        publisherRepository.save(savedPublisher);

        savedBook = Book.builder()
                .isbn("9786045629870")
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .category(savedCategory)
                .publisher(savedPublisher)
                .authors(List.of(savedAuthor))
                .originalPrice(79000L)
                .salePrice(63200L)
                .stockQuantity(84)
                .lowStockThreshold(10)
                .soldCount(5320)
                .avgRating(4.7)
                .reviewCount(1)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .description("<p>Hành trình theo đuổi vận mệnh...</p>")
                .images(new ArrayList<>())
                .build();
        bookRepository.save(savedBook);

        // Account & Customer for wishlist testing
        Account account = accountRepository.findByUsername("0933333333").orElseGet(() -> {
            Account acc = Account.builder()
                    .username("0933333333")
                    .passwordHash(passwordEncoder.encode("Password123"))
                    .role(Role.CUSTOMER)
                    .status(AccountStatus.ACTIVE)
                    .tokenVersion(0)
                    .build();
            return accountRepository.save(acc);
        });

        savedCustomer = customerRepository.findByAccountId(account.getId()).orElseGet(() -> {
            Customer cust = Customer.builder()
                    .account(account)
                    .fullName("Nguyễn Văn A")
                    .phone("0933333333")
                    .gender(Gender.MALE)
                    .customerTier(CustomerTier.SILVER)
                    .build();
            return customerRepository.save(cust);
        });

        customerToken = jwtTokenProvider.generateAccessToken(account);
    }

    @Test
    @DisplayName("GET /api/v1/books - Tìm kiếm & lọc sách thành công (200 OK)")
    void testSearchBooks_Success() throws Exception {
        mockMvc.perform(get("/api/v1/books")
                        .param("keyword", "Nhà Giả Kim")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].slug").value("nha-gia-kim"))
                .andExpect(jsonPath("$.data.items[0].originalPrice").value(79000))
                .andExpect(jsonPath("$.data.items[0].salePrice").value(63200))
                .andExpect(jsonPath("$.data.items[0].stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.data.facets.languages").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/books - Thất bại do kích thước trang quá lớn (400 BAD_REQUEST)")
    void testSearchBooks_InvalidSize() throws Exception {
        mockMvc.perform(get("/api/v1/books")
                        .param("size", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("GET /api/v1/books - Thất bại do khoảng giá không hợp lệ (400 BAD_REQUEST)")
    void testSearchBooks_InvalidPriceRange() throws Exception {
        mockMvc.perform(get("/api/v1/books")
                        .param("priceFrom", "200000")
                        .param("priceTo", "100000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("GET /api/v1/books/best-sellers - Thành công (200 OK)")
    void testGetBestSellers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/books/best-sellers?limit=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("nha-gia-kim"))
                .andExpect(jsonPath("$.data[0].soldCount").value(5320));
    }

    @Test
    @DisplayName("GET /api/v1/books/new-arrivals - Thành công (200 OK)")
    void testGetNewArrivals_Success() throws Exception {
        mockMvc.perform(get("/api/v1/books/new-arrivals?limit=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].slug").value("nha-gia-kim"));
    }

    @Test
    @DisplayName("GET /api/v1/books/suggest - Thành công trả về autocomplete (200 OK)")
    void testSuggest_Success() throws Exception {
        mockMvc.perform(get("/api/v1/books/suggest?q=Nhà")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.books").isArray())
                .andExpect(jsonPath("$.data.keywords").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/books/suggest - Thất bại do từ khóa dưới 2 ký tự (400 BAD_REQUEST)")
    void testSuggest_TooShort() throws Exception {
        mockMvc.perform(get("/api/v1/books/suggest?q=a")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{slug} - Thành công xem chi tiết sách khách vãng lai (200 OK)")
    void testGetBookDetail_Anonymous() throws Exception {
        mockMvc.perform(get("/api/v1/books/nha-gia-kim")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Nhà Giả Kim"))
                .andExpect(jsonPath("$.data.isInWishlist").value(false))
                .andExpect(jsonPath("$.data.stockStatus").value("IN_STOCK"))
                .andExpect(jsonPath("$.data.maxPurchasableQuantity").value(84))
                .andExpect(jsonPath("$.data.ratingSummary.avgRating").value(4.7));
    }

    @Test
    @DisplayName("GET /api/v1/books/{slug} - Thành công nhận diện sách trong Wishlist với Bearer Token (200 OK)")
    void testGetBookDetail_WithToken_InWishlist() throws Exception {
        // Thêm sách vào wishlist
        wishlistRepository.save(Wishlist.builder()
                .id(new WishlistId(savedCustomer.getId(), savedBook.getId()))
                .build());

        mockMvc.perform(get("/api/v1/books/nha-gia-kim")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isInWishlist").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/books/{slug} - Không tìm thấy cuốn sách (404 NOT_FOUND)")
    void testGetBookDetail_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/books/unknown-book")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/books/{slug}/related - Thành công lấy sách liên quan (200 OK)")
    void testGetRelatedBooks_Success() throws Exception {
        mockMvc.perform(get("/api/v1/books/nha-gia-kim/related?limit=5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/books/{slug}/reviews - Thành công lấy danh sách đánh giá có mask tên (200 OK)")
    void testGetBookReviews_Success() throws Exception {
        reviewRepository.save(Review.builder()
                .book(savedBook)
                .customer(savedCustomer)
                .orderId(12345L)
                .rating(5)
                .content("Sách đóng gói rất cẩn thận và chất lượng tuyệt vời!")
                .status(com.chungpr0.bookhub.common.enums.ReviewStatus.VISIBLE)
                .build());

        mockMvc.perform(get("/api/v1/books/nha-gia-kim/reviews?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].rating").value(5))
                .andExpect(jsonPath("$.data.items[0].customer.displayName").value("Nguyễn V. A."));
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

