package com.chungpr0.bookhub.modules.cart;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import com.chungpr0.bookhub.modules.cart.entity.CartItem;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminCartControllerTest {

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

    private String adminToken;
    private String customerToken;
    private Cart savedCart;

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

        // 1. Setup Admin Account
        Account adminAccount = Account.builder()
                .username("0900000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(adminAccount);
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount.getId(), adminAccount.getRole(), adminAccount.getTokenVersion());

        // 2. Setup Customer Account & Cart
        Account customerAccount = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(customerAccount);
        customerToken = jwtTokenProvider.generateAccessToken(customerAccount.getId(), customerAccount.getRole(), customerAccount.getTokenVersion());

        Customer customer = Customer.builder()
                .account(customerAccount)
                .fullName("Nguyễn Văn A")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customer = customerRepository.save(customer);

        savedCart = Cart.builder()
                .customer(customer)
                .updatedAt(OffsetDateTime.now())
                .build();
        savedCart = cartRepository.save(savedCart);

        Category category = Category.builder().name("Văn học").slug("van-hoc").sortOrder(0).build();
        category = categoryRepository.save(category);

        Publisher publisher = Publisher.builder().name("NXB Trẻ").slug("nxb-tre").build();
        publisher = publisherRepository.save(publisher);

        Book book = Book.builder()
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .category(category)
                .publisher(publisher)
                .originalPrice(100000L)
                .salePrice(80000L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(50)
                .build();
        book = bookRepository.save(book);

        CartItem cartItem = CartItem.builder()
                .cart(savedCart)
                .book(book)
                .quantity(3)
                .build();
        cartItemRepository.save(cartItem);
    }

    @Test
    @DisplayName("GET /api/v1/admin/carts - Admin truy cập thành công và lọc bằng JPA Specification (200 OK)")
    void testSearchCarts_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/carts")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("keyword", "Nguyễn Văn A")
                        .param("hasItems", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].customerName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.data.items[0].totalQuantity").value(3))
                .andExpect(jsonPath("$.data.items[0].subtotal").value(240000));
    }

    @Test
    @DisplayName("GET /api/v1/admin/carts - Customer truy cập bị chặn bởi PreAuthorize 403 ACCESS_DENIED")
    void testSearchCarts_Customer_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/carts")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/carts/{cartId} - Admin xem chi tiết giỏ hàng thành công (200 OK)")
    void testGetCartById_Admin_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/carts/" + savedCart.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cartId").value(savedCart.getId()))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.summary.subtotal").value(240000));
    }
}

