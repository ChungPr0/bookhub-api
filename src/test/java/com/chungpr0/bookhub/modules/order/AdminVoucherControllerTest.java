package com.chungpr0.bookhub.modules.order;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.order.dto.request.CreateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ToggleVoucherStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdatePaymentMethodRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateVoucherRequest;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentMethodRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminVoucherControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherUsageRepository voucherUsageRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

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
    private Voucher testVoucher;
    private PaymentMethod codMethod;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        // Admin Account
        Account admin = Account.builder()
                .username("0900000001")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        admin = accountRepository.save(admin);
        adminToken = jwtTokenProvider.generateAccessToken(admin.getId(), admin.getRole(), admin.getTokenVersion());

        // Customer Account
        Account customer = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customer = accountRepository.save(customer);
        customerToken = jwtTokenProvider.generateAccessToken(customer.getId(), customer.getRole(), customer.getTokenVersion());

        // Test Voucher
        testVoucher = Voucher.builder()
                .code("SUMMER2026")
                .name("Giảm hè rực rỡ")
                .description("Giảm 10% đơn từ 200k")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(10L)
                .maxDiscountAmount(50_000L)
                .minOrderAmount(200_000L)
                .usageLimit(100)
                .usageLimitPerCustomer(2)
                .usedCount(0)
                .startDate(OffsetDateTime.now().minusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(30))
                .status(VoucherStatus.ACTIVE)
                .build();
        testVoucher = voucherRepository.save(testVoucher);

        // Payment Method
        codMethod = paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.COD)
                .name("COD")
                .description("Tiền mặt khi nhận hàng")
                .isActive(true)
                .sortOrder(1)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/vouchers - Phân quyền: ADMIN 200 OK, CUSTOMER 403 Forbidden")
    void testSearchVouchers_Security() throws Exception {
        mockMvc.perform(get("/api/v1/admin/vouchers")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/vouchers")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1));
    }

    @Test
    @DisplayName("POST /api/v1/admin/vouchers - ADMIN tạo mới voucher thành công 201 Created")
    void testCreateVoucher_Success() throws Exception {
        CreateVoucherRequest request = CreateVoucherRequest.builder()
                .code("FLASH50")
                .name("Flash Sale 50k")
                .description("Giảm ngay 50.000đ cho đơn từ 300.000đ")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(50_000L)
                .minOrderAmount(300_000L)
                .usageLimit(500)
                .usageLimitPerCustomer(1)
                .startDate(OffsetDateTime.now().plusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(5))
                .build();

        mockMvc.perform(post("/api/v1/admin/vouchers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.code").value("FLASH50"))
                .andExpect(jsonPath("$.data.discountValue").value(50_000L));
    }

    @Test
    @DisplayName("POST /api/v1/admin/vouchers - Báo lỗi 409 khi mã voucher đã tồn tại")
    void testCreateVoucher_DuplicateCode_Throws409() throws Exception {
        CreateVoucherRequest request = CreateVoucherRequest.builder()
                .code("SUMMER2026") // Trùng mã đã có
                .name("Trùng hè")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(20_000L)
                .startDate(OffsetDateTime.now().plusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(5))
                .build();

        mockMvc.perform(post("/api/v1/admin/vouchers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONCURRENT_MODIFICATION"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/vouchers/{id} - ADMIN sửa đổi voucher thành công")
    void testUpdateVoucher_Success() throws Exception {
        UpdateVoucherRequest request = UpdateVoucherRequest.builder()
                .name("Giảm hè rực rỡ update")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(15L)
                .maxDiscountAmount(60_000L)
                .minOrderAmount(250_000L)
                .expirationDate(OffsetDateTime.now().plusDays(40))
                .build();

        mockMvc.perform(put("/api/v1/admin/vouchers/" + testVoucher.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.discountValue").value(15L));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/vouchers/{id}/status - Bật/tắt trạng thái voucher")
    void testToggleVoucherStatus_Success() throws Exception {
        ToggleVoucherStatusRequest request = ToggleVoucherStatusRequest.builder()
                .status(VoucherStatus.INACTIVE)
                .build();

        mockMvc.perform(patch("/api/v1/admin/vouchers/" + testVoucher.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/vouchers/{id} - Xóa voucher thành công khi chưa từng dùng")
    void testDeleteVoucher_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/vouchers/" + testVoucher.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/payment-methods/{id} - Chặn vô hiệu hóa phương thức cuối cùng (422 LAST_PAYMENT_METHOD_PROTECTION)")
    void testUpdatePaymentMethod_LastActiveProtection_Throws422() throws Exception {
        // Chỉ có 1 phương thức COD active -> cố tình tắt đi
        UpdatePaymentMethodRequest request = UpdatePaymentMethodRequest.builder()
                .isActive(false)
                .build();

        mockMvc.perform(patch("/api/v1/admin/payment-methods/" + codMethod.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.code").value("LAST_PAYMENT_METHOD_PROTECTION"));
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
        pointTransactionRepository.deleteAll();
        wishlistRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        addressRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();
    }
}
