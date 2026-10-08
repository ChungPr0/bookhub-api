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
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.order.dto.request.CheckoutPreviewRequest;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.entity.ShippingConfig;
import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CheckoutControllerTest {

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
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherUsageRepository voucherUsageRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ShippingConfigRepository shippingConfigRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Customer savedCustomer;
    private String customerToken;
    private Book savedBook;
    private Address savedAddress;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // 1. Base Payment Methods
        paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.COD)
                .name("Thanh toán khi nhận hàng (COD)")
                .description("Nhận hàng rồi mới thanh toán tiền mặt")
                .sortOrder(1)
                .isActive(true)
                .build());

        paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.VNPAY)
                .name("Cổng thanh toán VNPAY")
                .description("Thanh toán qua VNPAY QR hoặc ATM/Visa")
                .sortOrder(2)
                .isActive(true)
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

        // 3. Category & Publisher & Book
        Category cat = categoryRepository.save(Category.builder().name("Kinh tế").slug("kinh-te").sortOrder(0).build());
        Publisher pub = publisherRepository.save(Publisher.builder().name("NXB Trẻ").slug("nxb-tre").build());

        savedBook = bookRepository.save(Book.builder()
                .title("Kinh tế học hài hước")
                .slug("kinh-te-hoc-hai-huoc")
                .category(cat)
                .publisher(pub)
                .originalPrice(150_000L)
                .salePrice(120_000L)
                .coverType(CoverType.PAPERBACK)
                .status(BookStatus.ACTIVE)
                .stockQuantity(20)
                .weightGram(350)
                .build());

        // 4. Customer Account
        Account account = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        account = accountRepository.save(account);
        customerToken = jwtTokenProvider.generateAccessToken(account.getId(), account.getRole(), account.getTokenVersion());

        savedCustomer = Customer.builder()
                .account(account)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .rewardPoints(100)
                .totalSpent(0L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        savedCustomer = customerRepository.save(savedCustomer);

        // 5. Customer Address
        savedAddress = addressRepository.save(Address.builder()
                .customer(savedCustomer)
                .receiverName("Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 10 Dịch Vọng")
                .isDefault(true)
                .build());

        // 6. Cart & CartItem
        Cart cart = cartRepository.save(Cart.builder().customer(savedCustomer).build());
        cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .book(savedBook)
                .quantity(2) // 2 x 120k = 240k
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/payment-methods - Công khai lấy danh sách phương thức thanh toán đang kích hoạt")
    void testGetActivePaymentMethods() throws Exception {
        mockMvc.perform(get("/api/v1/payment-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("COD"))
                .andExpect(jsonPath("$.data[1].code").value("VNPAY"));
    }

    @Test
    @DisplayName("GET /api/v1/me/vouchers/available - Khách hàng lấy danh sách voucher khả dụng và không khả dụng")
    void testGetAvailableVouchers() throws Exception {
        // Voucher 1: Khả dụng (Min 200k, đơn 240k)
        voucherRepository.save(Voucher.builder()
                .code("GIAM10")
                .name("Giảm 10%")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(10L)
                .maxDiscountAmount(30_000L)
                .minOrderAmount(200_000L)
                .usageLimit(100)
                .usageLimitPerCustomer(2)
                .usedCount(0)
                .startDate(OffsetDateTime.now().minusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build());

        // Voucher 2: Không khả dụng (Min 500k > 240k)
        voucherRepository.save(Voucher.builder()
                .code("VIP500")
                .name("Giảm 50k đơn 500k")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(50_000L)
                .minOrderAmount(500_000L)
                .usageLimit(50)
                .usageLimitPerCustomer(1)
                .usedCount(0)
                .startDate(OffsetDateTime.now().minusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build());

        mockMvc.perform(get("/api/v1/me/vouchers/available")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("subtotal", "240000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.usable.length()").value(1))
                .andExpect(jsonPath("$.data.usable[0].code").value("GIAM10"))
                .andExpect(jsonPath("$.data.usable[0].estimatedDiscount").value(24_000L)) // 10% của 240k
                .andExpect(jsonPath("$.data.unusable.length()").value(1))
                .andExpect(jsonPath("$.data.unusable[0].voucher.code").value("VIP500"))
                .andExpect(jsonPath("$.data.unusable[0].reasonCode").value("VOUCHER_MIN_ORDER_NOT_MET"));
    }

    @Test
    @DisplayName("POST /api/v1/me/checkout/preview - Tính toán bảng báo giá xem trước đơn hàng chuẩn xác")
    void testCheckoutPreview_Success() throws Exception {
        // Voucher 10%
        voucherRepository.save(Voucher.builder()
                .code("GIAM10")
                .name("Giảm 10%")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(10L)
                .maxDiscountAmount(30_000L)
                .minOrderAmount(200_000L)
                .usageLimit(100)
                .usageLimitPerCustomer(2)
                .usedCount(0)
                .startDate(OffsetDateTime.now().minusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build());

        CheckoutPreviewRequest request = CheckoutPreviewRequest.builder()
                .bookIds(List.of(savedBook.getId()))
                .addressId(savedAddress.getId())
                .voucherCode("GIAM10")
                .pointsToUse(50) // Dùng 50 điểm = 50.000 VND
                .build();

        // 2 cuốn x 120k = 240k subtotal
        // Voucher 10% của 240k = 24k discount -> net = 216k
        // Điểm thưởng: 50 điểm x 100 VND = 5.000 VND discount
        // Phí ship Hà Nội = 30k
        // Final amount = 240k - 24k - 5k + 30k = 241k
        mockMvc.perform(post("/api/v1/me/checkout/preview")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.pricing.subtotal").value(240_000L))
                .andExpect(jsonPath("$.data.pricing.voucherDiscount").value(24_000L))
                .andExpect(jsonPath("$.data.pricing.pointsDiscount").value(5_000L))
                .andExpect(jsonPath("$.data.pricing.shippingFee").value(30_000L))
                .andExpect(jsonPath("$.data.pricing.finalAmount").value(241_000L))
                .andExpect(jsonPath("$.data.items.length()").value(1));
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
