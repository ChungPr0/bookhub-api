package com.chungpr0.bookhub.modules.report;

import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.report.dto.response.DashboardRealtimeResponse;
import com.chungpr0.bookhub.modules.report.dto.response.DashboardSummaryResponse;
import com.chungpr0.bookhub.modules.report.service.impl.AdminDashboardServiceImpl;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    private static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.ofHours(7);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private Book testBook;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(1L)
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .stockQuantity(50)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .id(10L)
                .book(testBook)
                .quantity(2)
                .unitPrice(100000L)
                .lineTotal(200000L)
                .costAmount(120000L)
                .build();

        testOrder = Order.builder()
                .id(100L)
                .orderCode("ORD-100")
                .status(OrderStatus.COMPLETED)
                .finalAmount(220000L)
                .shippingFee(20000L)
                .createdAt(OffsetDateTime.now(VIETNAM_OFFSET))
                .completedAt(OffsetDateTime.now(VIETNAM_OFFSET))
                .orderDetails(new ArrayList<>(List.of(detail)))
                .build();
    }

    @Test
    @DisplayName("getDashboardSummary: tính toán chỉ số hôm nay (TODAY) thành công")
    void getDashboardSummary_Today_Success() {
        when(orderRepository.findByStatusAndCompletedAtBetween(eq(OrderStatus.COMPLETED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(testOrder));
        when(orderRepository.findByCreatedAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(testOrder));
        when(customerRepository.countByCreatedAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(5L);

        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(2L);
        when(orderRepository.countByPaymentMethodCodeAndPaymentStatus(PaymentMethodCode.BANK_TRANSFER, PaymentStatus.UNPAID)).thenReturn(1L);
        when(orderRepository.countByPaymentStatus(PaymentStatus.REFUND_PENDING)).thenReturn(0L);
        when(bookRepository.countByStockQuantityLessThanEqualAndStockQuantityGreaterThan(10, 0)).thenReturn(3L);
        when(bookRepository.countByStockQuantityLessThanEqual(0)).thenReturn(1L);
        when(reviewRepository.countByAdminReplyIsNull()).thenReturn(4L);

        DashboardSummaryResponse response = adminDashboardService.getDashboardSummary("TODAY");

        assertThat(response.getPeriod().getType()).isEqualTo("TODAY");
        assertThat(response.getRevenue().getValue()).isEqualTo(200000L); // 220000 - 20000
        assertThat(response.getOrders().getTotal()).isEqualTo(1L);
        assertThat(response.getNewCustomers().getValue()).isEqualTo(5L);
        assertThat(response.getPendingActions().getPendingOrders()).isEqualTo(2L);
        assertThat(response.getTopBooks()).hasSize(1);
        assertThat(response.getTopBooks().get(0).getBook().getTitle()).isEqualTo("Nhà Giả Kim");
        assertThat(response.getTopBooks().get(0).getQuantitySold()).isEqualTo(2);
    }

    @Test
    @DisplayName("getDashboardRealtime: tính toán dữ liệu vận hành thời gian thực thành công")
    void getDashboardRealtime_Success() {
        when(orderRepository.findByCreatedAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(testOrder));

        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(1L);
        when(orderRepository.countByStatus(OrderStatus.CONFIRMED)).thenReturn(2L);
        when(orderRepository.countByPaymentMethodCodeAndPaymentStatus(PaymentMethodCode.BANK_TRANSFER, PaymentStatus.UNPAID)).thenReturn(0L);
        when(orderRepository.countByStatus(OrderStatus.RETURNED)).thenReturn(0L);

        DashboardRealtimeResponse response = adminDashboardService.getDashboardRealtime();

        assertThat(response.getTodaySummary().getTodayGrossRevenue()).isEqualTo(220000L);
        assertThat(response.getTodaySummary().getTodayNetRevenue()).isEqualTo(200000L);
        assertThat(response.getTodaySummary().getTodayOrdersCount()).isEqualTo(1L);
        assertThat(response.getTodaySummary().getTodayCompletedCount()).isEqualTo(1L);
        assertThat(response.getHourlyBreakdown()).isNotEmpty();
        assertThat(response.getUrgentQueues().getUnconfirmedOrdersCount()).isEqualTo(1L);
    }
}

