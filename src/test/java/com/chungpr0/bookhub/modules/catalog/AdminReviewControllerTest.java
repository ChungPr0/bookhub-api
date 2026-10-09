package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewReplyRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewVisibilityRequest;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.entity.Review;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentMethodRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminReviewControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private CustomerRepository customerRepository;

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
    private Review savedReview;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Account customerAccount = accountRepository.findByUsername("0918888888").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0918888888")
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

        Customer customer = customerRepository.save(Customer.builder()
                .account(customerAccount)
                .fullName("Trần Thị Mai")
                .email("mai.tran@gmail.com")
                .phone("0918888888")
                .gender(Gender.FEMALE)
                .customerTier(CustomerTier.BRONZE)
                .rewardPoints(0)
                .build());

        Category category = categoryRepository.save(Category.builder().name("Tâm lý").slug("tam-ly").build());
        Author author = authorRepository.save(Author.builder().name("Dale Carnegie").slug("dale-carnegie").build());
        Publisher publisher = publisherRepository.save(Publisher.builder().name("NXB Tổng Hợp").slug("nxb-tong-hop").build());

        Book savedBook = bookRepository.save(Book.builder()
                .title("Đắc Nhân Tâm")
                .slug("dac-nhan-tam")
                .category(category)
                .publisher(publisher)
                .authors(List.of(author))
                .originalPrice(90000L)
                .salePrice(72000L)
                .stockQuantity(100)
                .lowStockThreshold(10)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .images(new ArrayList<>())
                .build());

        PaymentMethod paymentMethod = paymentMethodRepository.save(PaymentMethod.builder()
                .code(PaymentMethodCode.COD)
                .name("COD")
                .description("Thanh toán tiền mặt")
                .isActive(true)
                .sortOrder(1)
                .build());

        Order savedOrder = Order.builder()
                .orderCode("ORD-20261009-TEST02")
                .customer(customer)
                .paymentMethod(paymentMethod)
                .receiverName("Trần Thị Mai")
                .receiverPhone("0918888888")
                .shippingAddress("123 Lê Lợi, Q.1, TP.HCM")
                .subtotalAmount(72000L)
                .shippingFee(20000L)
                .finalAmount(92000L)
                .status(OrderStatus.COMPLETED)
                .paymentStatus(PaymentStatus.PAID)
                .completedAt(OffsetDateTime.now().minusDays(3))
                .build();

        OrderDetail detail = OrderDetail.builder()
                .order(savedOrder)
                .book(savedBook)
                .bookTitle(savedBook.getTitle())
                .quantity(1)
                .unitPrice(72000L)
                .lineTotal(72000L)
                .build();

        savedOrder.setOrderDetails(new ArrayList<>(List.of(detail)));
        orderRepository.save(savedOrder);

        savedReview = reviewRepository.save(Review.builder()
                .customer(customer)
                .book(savedBook)
                .orderId(savedOrder.getId())
                .rating(5)
                .content("Sách rất hay và chất lượng tốt!")
                .status(ReviewStatus.VISIBLE)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/reviews - 401 UNAUTHORIZED khi chưa đăng nhập")
    void testGetAdminReviews_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reviews"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/reviews - 403 FORBIDDEN khi người dùng là CUSTOMER")
    void testGetAdminReviews_ForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reviews")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/reviews - 200 OK tra cứu và lọc danh sách đánh giá cho ADMIN")
    void testGetAdminReviews_Success() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("rating", "5")
                        .param("status", "VISIBLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].rating").value(5))
                .andExpect(jsonPath("$.data.items[0].status").value("VISIBLE"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/reviews/{id}/visibility - 200 OK ẩn bài đánh giá với lý do kiểm duyệt")
    void testUpdateReviewVisibility_Hide() throws Exception {
        AdminReviewVisibilityRequest request = AdminReviewVisibilityRequest.builder()
                .status(ReviewStatus.HIDDEN)
                .reason("Chứa từ ngữ quảng cáo ngoài luồng")
                .build();

        mockMvc.perform(patch("/api/v1/admin/reviews/" + savedReview.getId() + "/visibility")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/reviews/{id}/reply - 200 OK phản hồi chính thức từ cửa hàng")
    void testReplyReview_Success() throws Exception {
        AdminReviewReplyRequest request = AdminReviewReplyRequest.builder()
                .content("Cảm ơn bạn đã tin tưởng và ủng hộ nhà sách BookHub!")
                .build();

        mockMvc.perform(put("/api/v1/admin/reviews/" + savedReview.getId() + "/reply")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/reviews/{id}/reply - 200 OK xóa phản hồi của cửa hàng")
    void testDeleteReviewReply_Success() throws Exception {
        savedReview.setAdminReply("Phản hồi cũ");
        reviewRepository.save(savedReview);

        mockMvc.perform(delete("/api/v1/admin/reviews/" + savedReview.getId() + "/reply")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        reviewRepository.deleteAll();
        orderStatusHistoryRepository.deleteAll();
        paymentRepository.deleteAll();
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        paymentMethodRepository.deleteAll();
        wishlistRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        authorRepository.deleteAll();
        publisherRepository.deleteAll();
        customerRepository.deleteAll();
    }
}
