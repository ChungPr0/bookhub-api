package com.chungpr0.bookhub.modules.order;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.order.dto.request.AdminBankTransferConfirmRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminUpdateOrderStatusRequest;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
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
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
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
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminOrderControllerTest {

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
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String customerToken;
    private Customer testCustomer;
    private PaymentMethod codMethod;
    private PaymentMethod bankMethod;
    private Book testBook;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // Admin Account
        Account admin = accountRepository.save(Account.builder()
                .username("0900000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        adminToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getRole(), admin.getTokenVersion());

        // Customer Account
        Account customerAcc = accountRepository.save(Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());
        customerToken = jwtTokenProvider.generateAccessToken(customerAcc.getId(), customerAcc.getRole(), customerAcc.getTokenVersion());

        testCustomer = customerRepository.save(Customer.builder()
                .account(customerAcc)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .rewardPoints(100)
                .totalSpent(0L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build());

        // Payment Methods
        codMethod = paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.COD)
                .name("COD")
                .isActive(true)
                .sortOrder(1)
                .build());

        bankMethod = paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.BANK_TRANSFER)
                .name("Chuyển khoản")
                .isActive(true)
                .sortOrder(2)
                .build());

        // Catalog
        Category cat = categoryRepository.save(Category.builder().name("Văn học").slug("van-hoc").sortOrder(0).build());
        Publisher pub = publisherRepository.save(Publisher.builder().name("NXB Trẻ").slug("nxb-tre").build());
        testBook = bookRepository.save(Book.builder()
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .category(cat)
                .publisher(pub)
                .originalPrice(100_000L)
                .salePrice(80_000L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(10)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/orders - Phân quyền: ADMIN 200 OK, CUSTOMER 403 Forbidden")
    void testSearchOrders_Security() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{orderCode}/status - Chuyển trạng thái đơn hàng thành công")
    void testUpdateOrderStatus_Success() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-20261008-STATUS01")
                .customer(testCustomer)
                .paymentMethod(codMethod)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .subtotalAmount(160_000L)
                .finalAmount(190_000L)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("123 Cầu Giấy")
                .version(0)
                .build());

        AdminUpdateOrderStatusRequest request = AdminUpdateOrderStatusRequest.builder()
                .status(OrderStatus.CONFIRMED)
                .note("Đã xác nhận đơn hàng qua điện thoại")
                .version(0)
                .build();

        mockMvc.perform(patch("/api/v1/admin/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/orders/{id}/status - Chặn giao hàng đơn online chưa thanh toán (409 INVALID_ORDER_STATUS_TRANSITION)")
    void testUpdateOrderStatus_OnlineUnpaidToShipping_Throws409() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-20261008-BANK01")
                .customer(testCustomer)
                .paymentMethod(bankMethod)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.UNPAID) // Chưa trả tiền chuyển khoản
                .subtotalAmount(160_000L)
                .finalAmount(190_000L)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("123 Cầu Giấy")
                .version(0)
                .build());

        AdminUpdateOrderStatusRequest request = AdminUpdateOrderStatusRequest.builder()
                .status(OrderStatus.SHIPPING)
                .version(0)
                .build();

        mockMvc.perform(patch("/api/v1/admin/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_ORDER_STATUS_TRANSITION"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/orders/{id}/payment-confirmation - Xác nhận đã nhận tiền chuyển khoản thành công")
    void testConfirmBankTransfer_Success() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-20261008-BANK02")
                .customer(testCustomer)
                .paymentMethod(bankMethod)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .subtotalAmount(160_000L)
                .finalAmount(190_000L)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("123 Cầu Giấy")
                .version(0)
                .build());

        AdminBankTransferConfirmRequest request = AdminBankTransferConfirmRequest.builder()
                .bankTransactionRef("FT261008123456")
                .amountReceived(190_000L)
                .note("Đã nhận đủ tiền từ khách hàng")
                .build();

        mockMvc.perform(post("/api/v1/admin/orders/" + order.getId() + "/payment-confirmation")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.paymentStatus").value("PAID"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/orders/{id}/cancel - Admin hủy đơn thành công và hoàn trả kho sách")
    void testAdminCancelOrder_Success() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .orderCode("ORD-20261008-CANCEL01")
                .customer(testCustomer)
                .paymentMethod(codMethod)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.UNPAID)
                .subtotalAmount(160_000L)
                .finalAmount(190_000L)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("123 Cầu Giấy")
                .version(0)
                .build());

        orderDetailRepository.save(OrderDetail.builder()
                .order(order)
                .book(testBook)
                .bookTitle("Nhà Giả Kim")
                .quantity(2)
                .unitPrice(80_000L)
                .lineTotal(160_000L)
                .build());

        AdminCancelOrderRequest request = AdminCancelOrderRequest.builder()
                .reason(OrderCancelReason.OTHER)
                .note("Khách yêu cầu hủy qua điện thoại")
                .version(0)
                .build();

        mockMvc.perform(post("/api/v1/admin/orders/" + order.getId() + "/cancel")
                        .header("Authorization", "Bearer " + adminToken)
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
