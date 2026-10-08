package com.chungpr0.bookhub.modules.order;

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
import com.chungpr0.bookhub.modules.order.dto.request.CreateOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CustomerCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.entity.ShippingConfig;
import com.chungpr0.bookhub.modules.order.enums.OrderCancelReason;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentMethodRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentRepository;
import com.chungpr0.bookhub.modules.order.repository.ShippingConfigRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderControllerTest {

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
    private AddressRepository addressRepository;

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
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherUsageRepository voucherUsageRepository;

    @Autowired
    private ShippingConfigRepository shippingConfigRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Customer customerA;
    private String tokenA;
    private String tokenB;
    private Book savedBook;
    private Address savedAddress;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // 1. Payment Method
        paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.COD)
                .name("COD")
                .description("Thanh toán tiền mặt khi nhận hàng")
                .isActive(true)
                .sortOrder(1)
                .build());

        // 2. Shipping Config
        shippingConfigRepository.save(ShippingConfig.builder()
                .standardBaseFee(30_000L)
                .expressBaseFee(45_000L)
                .sameDayBaseFee(60_000L)
                .freeShippingThreshold(300_000L)
                .maxFreeShippingSubsidy(30_000L)
                .standardMaxWeightGram(2000)
                .overweightUnitGram(500)
                .overweightSurcharge(5_000L)
                .build());

        // 3. Books
        Category cat = categoryRepository.save(Category.builder().name("Văn học").slug("van-hoc").sortOrder(0).build());
        Publisher pub = publisherRepository.save(Publisher.builder().name("NXB Trẻ").slug("nxb-tre").build());
        savedBook = bookRepository.save(Book.builder()
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .category(cat)
                .publisher(pub)
                .originalPrice(100_000L)
                .salePrice(80_000L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(15)
                .weightGram(300)
                .build());

        // 4. Customer A
        Account accA = accountRepository.save(Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        tokenA = jwtTokenProvider.generateAccessToken(accA.getId(), accA.getRole(), accA.getTokenVersion());

        customerA = customerRepository.save(Customer.builder()
                .account(accA)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .rewardPoints(200)
                .totalSpent(0L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        savedAddress = addressRepository.save(Address.builder()
                .customer(customerA)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("123 Xuân Thủy")
                .isDefault(true)
                .build());

        Cart cartA = cartRepository.save(Cart.builder().customer(customerA).build());
        cartItemRepository.save(CartItem.builder()
                .cart(cartA)
                .book(savedBook)
                .quantity(2) // 2 x 80k = 160k
                .build());

        // 5. Customer B
        Account accB = accountRepository.save(Account.builder()
                .username("0977777777")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        tokenB = jwtTokenProvider.generateAccessToken(accB.getId(), accB.getRole(), accB.getTokenVersion());

        customerRepository.save(Customer.builder()
                .account(accB)
                .fullName("Trần Thị B")
                .phone("0977777777")
                .gender(Gender.FEMALE)
                .customerTier(CustomerTier.BRONZE)
                .rewardPoints(0)
                .totalSpent(0L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/me/orders - Báo lỗi 428 PRECONDITION_REQUIRED khi thiếu Idempotency-Key")
    void testCreateOrder_MissingIdempotencyKey_Throws428() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .bookIds(List.of(savedBook.getId()))
                .addressId(savedAddress.getId())
                .paymentMethod(PaymentMethodCode.COD)
                .expectedFinalAmount(190_000L)
                .build();

        mockMvc.perform(post("/api/v1/me/orders")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(428))
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REQUIRED"));
    }

    @Test
    @DisplayName("POST /api/v1/me/orders - Thành công đặt hàng COD 201 Created và trừ tồn kho")
    void testCreateOrder_Success_COD() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        // 2 cuốn x 80k = 160k + 30k ship = 190k
        CreateOrderRequest request = CreateOrderRequest.builder()
                .bookIds(List.of(savedBook.getId()))
                .addressId(savedAddress.getId())
                .paymentMethod(PaymentMethodCode.COD)
                .expectedFinalAmount(190_000L)
                .note("Giao giờ hành chính")
                .build();

        mockMvc.perform(post("/api/v1/me/orders")
                        .header("Authorization", "Bearer " + tokenA)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.order.orderCode").isNotEmpty())
                .andExpect(jsonPath("$.data.order.status").value("PENDING"))
                .andExpect(jsonPath("$.data.order.pricing.finalAmount").value(190_000L));
    }

    @Test
    @DisplayName("POST /api/v1/me/orders - Báo lỗi 409 PRICE_CHANGED khi lệch expectedFinalAmount")
    void testCreateOrder_PriceSlippage_Throws409() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .bookIds(List.of(savedBook.getId()))
                .addressId(savedAddress.getId())
                .paymentMethod(PaymentMethodCode.COD)
                .expectedFinalAmount(150_000L) // Kỳ vọng 150k nhưng tính ra 190k
                .build();

        mockMvc.perform(post("/api/v1/me/orders")
                        .header("Authorization", "Bearer " + tokenA)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRICE_CHANGED"));
    }

    @Test
    @DisplayName("GET /api/v1/me/orders/{orderCode} - Chặn IDOR: Trả về 404 khi xem đơn hàng của người khác")
    void testGetOrderDetail_IDOR_Throws404() throws Exception {
        // Đơn của Customer A
        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-20261008-CUSTOMER-A")
                .customer(customerA)
                .paymentMethod(paymentMethodRepository.findByCode(PaymentMethodCode.COD).orElseThrow())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("123 Cầu Giấy")
                .subtotalAmount(160_000L)
                .finalAmount(190_000L)
                .build());

        // Customer B xem đơn của Customer A -> Bị trả 404 ORDER_NOT_FOUND
        mockMvc.perform(get("/api/v1/me/orders/" + order.getOrderCode())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/me/orders/{orderCode}/cancel - Khách hàng tự hủy đơn PENDING thành công 200 OK")
    void testCancelOrder_Success() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-20261008-CANCELME")
                .customer(customerA)
                .paymentMethod(paymentMethodRepository.findByCode(PaymentMethodCode.COD).orElseThrow())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("123 Cầu Giấy")
                .subtotalAmount(160_000L)
                .finalAmount(190_000L)
                .build());

        CustomerCancelOrderRequest request = CustomerCancelOrderRequest.builder()
                .reason(OrderCancelReason.CHANGE_OF_MIND)
                .reasonNote("Thay đổi ý định")
                .build();

        mockMvc.perform(post("/api/v1/me/orders/" + order.getOrderCode() + "/cancel")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        orderStatusHistoryRepository.deleteAll();
        paymentRepository.deleteAll();
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        voucherUsageRepository.deleteAll();
        voucherRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        shippingConfigRepository.deleteAll();
        pointTransactionRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        wishlistRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();
    }
}
