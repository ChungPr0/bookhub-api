package com.chungpr0.bookhub.modules.cart;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.cart.dto.request.AddToCartRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeItemRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.UpdateCartItemQuantityRequest;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CartControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Customer customerA;
    private String tokenA;

    private Customer customerB;
    private String tokenB;

    private Book savedBook1;
    private Book savedBook2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        wishlistRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();

        Category category = Category.builder()
                .name("Văn học")
                .slug("van-hoc")
                .sortOrder(0)
                .build();
        category = categoryRepository.save(category);

        Publisher publisher = Publisher.builder()
                .name("NXB Hội Nhà Văn")
                .slug("nxb-hoi-nha-van")
                .build();
        publisher = publisherRepository.save(publisher);

        savedBook1 = Book.builder()
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .category(category)
                .publisher(publisher)
                .originalPrice(79000L)
                .salePrice(63200L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(50)
                .build();
        savedBook1 = bookRepository.save(savedBook1);

        savedBook2 = Book.builder()
                .title("Cây Cam Ngọt Của Tôi")
                .slug("cay-cam-ngot-cua-toi")
                .category(category)
                .publisher(publisher)
                .originalPrice(108000L)
                .salePrice(86400L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(5)
                .build();
        savedBook2 = bookRepository.save(savedBook2);

        // Setup Customer A
        Account accountA = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(accountA);

        customerA = Customer.builder()
                .account(accountA)
                .fullName("Khách hàng A")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customerRepository.save(customerA);

        tokenA = jwtTokenProvider.generateAccessToken(accountA.getId(), accountA.getRole(), accountA.getTokenVersion());

        // Setup Customer B
        Account accountB = Account.builder()
                .username("0977777777")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(accountB);

        customerB = Customer.builder()
                .account(accountB)
                .fullName("Khách hàng B")
                .phone("0977777777")
                .gender(Gender.FEMALE)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customerRepository.save(customerB);

        tokenB = jwtTokenProvider.generateAccessToken(accountB.getId(), accountB.getRole(), accountB.getTokenVersion());
    }

    @Test
    @DisplayName("GET /api/v1/me/cart - Thành công lấy giỏ hàng (200 OK)")
    void testGetCart_Success() throws Exception {
        mockMvc.perform(get("/api/v1/me/cart")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.summary.itemCount").value(0))
                .andExpect(jsonPath("$.data.summary.subtotal").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/me/cart - Chưa đăng nhập trả về 401 UNAUTHENTICATED")
    void testGetCart_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/me/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("POST /api/v1/me/cart/items - Thêm mới sản phẩm vào giỏ thành công (200 OK)")
    void testAddToCart_Success() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .bookId(savedBook1.getId())
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/me/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].bookId").value(savedBook1.getId()))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.summary.itemCount").value(1))
                .andExpect(jsonPath("$.data.summary.subtotal").value(126400));
    }

    @Test
    @DisplayName("POST /api/v1/me/cart/items - Validation thất bại khi số lượng < 1 (400 Bad Request)")
    void testAddToCart_ValidationFailed_QuantityZero() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .bookId(savedBook1.getId())
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/v1/me/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/v1/me/cart/items - Vượt quá tồn kho khả dụng trả về 422 INSUFFICIENT_STOCK")
    void testAddToCart_InsufficientStock() throws Exception {
        AddToCartRequest request = AddToCartRequest.builder()
                .bookId(savedBook2.getId()) // stock = 5
                .quantity(10)
                .build();

        mockMvc.perform(post("/api/v1/me/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.details.items").isArray());
    }

    @Test
    @DisplayName("PATCH /api/v1/me/cart/items/{bookId} - Cập nhật số lượng chốt cuối thành công (200 OK)")
    void testUpdateItemQuantity_Success() throws Exception {
        // Thêm trước
        AddToCartRequest addReq = AddToCartRequest.builder().bookId(savedBook1.getId()).quantity(1).build();
        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReq)));

        UpdateCartItemQuantityRequest patchReq = UpdateCartItemQuantityRequest.builder().quantity(4).build();
        mockMvc.perform(patch("/api/v1/me/cart/items/" + savedBook1.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].quantity").value(4))
                .andExpect(jsonPath("$.data.summary.subtotal").value(252800));
    }

    @Test
    @DisplayName("DELETE /api/v1/me/cart/items/{bookId} - Xóa một sản phẩm thành công (200 OK)")
    void testRemoveItem_Success() throws Exception {
        // Thêm trước
        AddToCartRequest addReq = AddToCartRequest.builder().bookId(savedBook1.getId()).quantity(1).build();
        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addReq)));

        mockMvc.perform(delete("/api/v1/me/cart/items/" + savedBook1.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/v1/me/cart/items - Xóa nhiều sản phẩm theo bookIds thành công (200 OK)")
    void testRemoveItems_Batch_Success() throws Exception {
        // Thêm 2 món
        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddToCartRequest.builder().bookId(savedBook1.getId()).quantity(1).build())));
        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddToCartRequest.builder().bookId(savedBook2.getId()).quantity(1).build())));

        mockMvc.perform(delete("/api/v1/me/cart/items")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("bookIds", savedBook1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/me/cart - Xóa sạch giỏ hàng thành công (200 OK)")
    void testClearCart_Success() throws Exception {
        // Thêm sách
        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddToCartRequest.builder().bookId(savedBook1.getId()).quantity(1).build())));

        mockMvc.perform(delete("/api/v1/me/cart")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.summary.itemCount").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/me/cart/merge - Gộp giỏ hàng vãng lai thành công với Smart Merge (200 OK)")
    void testMergeCart_Success() throws Exception {
        CartMergeRequest request = CartMergeRequest.builder()
                .items(List.of(
                        CartMergeItemRequest.builder().bookId(savedBook1.getId()).quantity(2).build(),
                        CartMergeItemRequest.builder().bookId(savedBook2.getId()).quantity(10).build() // Capped to 5
                ))
                .build();

        mockMvc.perform(post("/api/v1/me/cart/merge")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cart.items.length()").value(2))
                .andExpect(jsonPath("$.data.mergeReport.length()").value(2))
                .andExpect(jsonPath("$.data.mergeReport[0].result").value("MERGED"))
                .andExpect(jsonPath("$.data.mergeReport[1].result").value("ADJUSTED"))
                .andExpect(jsonPath("$.data.mergeReport[1].mergedQuantity").value(5));
    }

    @Test
    @DisplayName("IDOR Defense - Khách hàng A và B có giỏ hàng hoàn toàn độc lập")
    void testIdorDefense_CartIsolation() throws Exception {
        // Customer A adds book 1
        mockMvc.perform(post("/api/v1/me/cart/items")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AddToCartRequest.builder().bookId(savedBook1.getId()).quantity(1).build())));

        // Customer B checks their cart, should be empty
        mockMvc.perform(get("/api/v1/me/cart")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.summary.itemCount").value(0));
    }
}
