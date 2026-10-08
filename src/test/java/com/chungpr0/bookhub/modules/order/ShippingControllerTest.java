package com.chungpr0.bookhub.modules.order;

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
import com.chungpr0.bookhub.modules.order.dto.request.CalculateShippingRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ShippingAddressRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ShippingCalculateItemRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateShippingConfigRequest;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.entity.ShippingConfig;
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
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ShippingControllerTest {

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
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PublisherRepository publisherRepository;

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
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String customerToken;
    private Book savedBook;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // 1. Shipping Config
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

        // 2. Admin Account
        Account adminAccount = Account.builder()
                .username("0900000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        adminAccount = accountRepository.save(adminAccount);
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount.getId(), adminAccount.getRole(), adminAccount.getTokenVersion());

        // 3. Customer Account
        Account custAccount = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        custAccount = accountRepository.save(custAccount);
        customerToken = jwtTokenProvider.generateAccessToken(custAccount.getId(), custAccount.getRole(), custAccount.getTokenVersion());

        Customer customer = Customer.builder()
                .account(custAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .build();
        customer = customerRepository.save(customer);

        // 4. Book
        Category cat = categoryRepository.save(Category.builder().name("CNTT").slug("cntt").sortOrder(0).build());
        Publisher pub = publisherRepository.save(Publisher.builder().name("NXB Trẻ").slug("nxb-tre").build());
        savedBook = bookRepository.save(Book.builder()
                .title("Clean Architecture")
                .slug("clean-architecture")
                .category(cat)
                .publisher(pub)
                .originalPrice(200_000L)
                .salePrice(180_000L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(10)
                .weightGram(500)
                .build());

        // 5. Payment Method & Order
        PaymentMethod cod = paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.COD)
                .name("COD")
                .isActive(true)
                .sortOrder(1)
                .build());

        savedOrder = Order.builder()
                .orderCode("ORD-20261008-TRK001")
                .customer(customer)
                .paymentMethod(cod)
                .status(OrderStatus.SHIPPING)
                .paymentStatus(PaymentStatus.UNPAID)
                .subtotalAmount(180_000L)
                .shippingFee(30_000L)
                .finalAmount(210_000L)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("123 Xuân Thủy, Cầu Giấy, Hà Nội")
                .build();
        savedOrder.setCreatedAt(OffsetDateTime.now().minusDays(2));
        savedOrder.setConfirmedAt(OffsetDateTime.now().minusDays(1));
        savedOrder.setShippedAt(OffsetDateTime.now().minusHours(5));
        savedOrder = orderRepository.save(savedOrder);
    }

    @Test
    @DisplayName("GET /api/v1/shipping/services - Lấy danh sách dịch vụ giao hàng công khai")
    void testGetShippingServices() throws Exception {
        mockMvc.perform(get("/api/v1/shipping/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("POST /api/v1/shipping/calculate - Tính cước vận chuyển chuẩn xác theo trọng lượng và tỉnh thành")
    void testCalculateShipping() throws Exception {
        ShippingAddressRequest addressRequest = ShippingAddressRequest.builder()
                .fullName("Tiến Chung")
                .phone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("123 Cầu Giấy")
                .build();

        CalculateShippingRequest request = CalculateShippingRequest.builder()
                .shippingAddress(addressRequest)
                .items(List.of(
                        ShippingCalculateItemRequest.builder().bookId(savedBook.getId()).quantity(1).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/shipping/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.packageInfo.region").value("INNER_CITY"))
                .andExpect(jsonPath("$.data.packageInfo.totalWeightGram").value(500))
                .andExpect(jsonPath("$.data.services.length()").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/shipping/tracking/{orderCode} - Lấy hành trình vận đơn chi tiết")
    void testGetTracking() throws Exception {
        mockMvc.perform(get("/api/v1/shipping/tracking/" + savedOrder.getOrderCode())
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-20261008-TRK001"))
                .andExpect(jsonPath("$.data.currentStatus").value("SHIPPING"))
                .andExpect(jsonPath("$.data.events").isArray())
                .andExpect(jsonPath("$.data.events.length()").value(4)); // CREATED, PACKED, HANDED_OVER, IN_TRANSIT
    }

    @Test
    @DisplayName("GET /api/v1/admin/shipping/configs - Phân quyền: ADMIN truy cập thành công 200, CUSTOMER bị chặn 403")
    void testGetShippingConfigs_Security() throws Exception {
        // Customer bị chặn 403 Forbidden
        mockMvc.perform(get("/api/v1/admin/shipping/configs")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        // Admin được phép 200 OK
        mockMvc.perform(get("/api/v1/admin/shipping/configs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.standardBaseFee").value(30_000L));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/shipping/configs - ADMIN cập nhật cấu hình biểu phí cước giao hàng")
    void testUpdateShippingConfig() throws Exception {
        UpdateShippingConfigRequest request = UpdateShippingConfigRequest.builder()
                .standardBaseFee(35_000L)
                .expressBaseFee(50_000L)
                .sameDayBaseFee(70_000L)
                .freeShippingThreshold(350_000L)
                .maxFreeShippingSubsidy(35_000L)
                .standardMaxWeightGram(2500)
                .overweightUnitGram(500)
                .overweightSurcharge(6_000L)
                .build();

        mockMvc.perform(put("/api/v1/admin/shipping/configs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.standardBaseFee").value(35_000L))
                .andExpect(jsonPath("$.data.freeShippingThreshold").value(350_000L));
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
        wishlistRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        publisherRepository.deleteAll();
    }
}
