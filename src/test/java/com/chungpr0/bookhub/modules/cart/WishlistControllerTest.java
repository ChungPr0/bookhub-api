package com.chungpr0.bookhub.modules.cart;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.OffsetDateTime;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WishlistControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

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

    private Customer customer;
    private String token;
    private Book savedBook;

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
                .name("Kỹ năng")
                .slug("ky-nang")
                .sortOrder(0)
                .build();
        category = categoryRepository.save(category);

        Publisher publisher = Publisher.builder()
                .name("NXB Trẻ")
                .slug("nxb-tre")
                .build();
        publisher = publisherRepository.save(publisher);

        savedBook = Book.builder()
                .title("Đắc Nhân Tâm")
                .slug("dac-nhan-tam")
                .category(category)
                .publisher(publisher)
                .originalPrice(90000L)
                .salePrice(72000L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(100)
                .build();
        savedBook = bookRepository.save(savedBook);

        Account account = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(account);

        customer = Customer.builder()
                .account(account)
                .fullName("Khách hàng Test")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customerRepository.save(customer);

        token = jwtTokenProvider.generateAccessToken(account.getId(), account.getRole(), account.getTokenVersion());
    }

    @Test
    @DisplayName("GET /api/v1/me/wishlist - Lấy danh sách yêu thích thành công có phân trang (200 OK)")
    void testGetWishlist_Success() throws Exception {
        // Thêm vào wishlist
        mockMvc.perform(put("/api/v1/me/wishlist/" + savedBook.getId())
                .header("Authorization", "Bearer " + token));

        mockMvc.perform(get("/api/v1/me/wishlist")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(savedBook.getId()))
                .andExpect(jsonPath("$.data.items[0].title").value("Đắc Nhân Tâm"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    @DisplayName("PUT /api/v1/me/wishlist/{bookId} - Thêm sách vào yêu thích thành công Idempotent (200 OK)")
    void testAddToWishlist_Idempotent() throws Exception {
        // Lần 1
        mockMvc.perform(put("/api/v1/me/wishlist/" + savedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isInWishlist").value(true));

        // Lần 2 (idempotent)
        mockMvc.perform(put("/api/v1/me/wishlist/" + savedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isInWishlist").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/me/wishlist/{bookId} - Sách không tồn tại trả về 404 BOOK_NOT_FOUND")
    void testAddToWishlist_NotFound() throws Exception {
        mockMvc.perform(put("/api/v1/me/wishlist/9999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/v1/me/wishlist/{bookId} - Xóa sách khỏi yêu thích thành công Idempotent (200 OK)")
    void testRemoveFromWishlist_Idempotent() throws Exception {
        // Thêm trước
        mockMvc.perform(put("/api/v1/me/wishlist/" + savedBook.getId())
                .header("Authorization", "Bearer " + token));

        // Xóa lần 1
        mockMvc.perform(delete("/api/v1/me/wishlist/" + savedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isInWishlist").value(false));

        // Xóa lần 2 (idempotent)
        mockMvc.perform(delete("/api/v1/me/wishlist/" + savedBook.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isInWishlist").value(false));
    }
}
