package com.chungpr0.bookhub.modules.report;

import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.inventory.repository.BatchRepository;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.report.dto.request.CustomerReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.ExportReportRequest;
import com.chungpr0.bookhub.modules.report.dto.request.InventoryReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.RevenueReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.TopBooksFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.response.CustomerReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.InventoryReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.RevenueReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.TopBookReportResponse;
import com.chungpr0.bookhub.modules.report.service.impl.AdminReportServiceImpl;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    private static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.ofHours(7);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AdminReportServiceImpl adminReportService;

    private Book testBook;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        Category category = Category.builder().id(1L).name("Văn học").slug("van-hoc").build();
        testBook = Book.builder()
                .id(1L)
                .title("Nhà Giả Kim")
                .isbn("9786045892345")
                .salePrice(100000L)
                .stockQuantity(50)
                .category(category)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .id(10L)
                .book(testBook)
                .quantity(3)
                .unitPrice(100000L)
                .lineTotal(300000L)
                .costAmount(180000L)
                .build();

        testOrder = Order.builder()
                .id(100L)
                .orderCode("ORD-100")
                .status(OrderStatus.COMPLETED)
                .subtotalAmount(300000L)
                .finalAmount(320000L)
                .shippingFee(20000L)
                .voucherDiscount(0L)
                .pointsDiscount(0L)
                .completedAt(LocalDate.of(2026, 10, 5).atTime(10, 0).atOffset(VIETNAM_OFFSET))
                .orderDetails(new ArrayList<>(List.of(detail)))
                .build();
    }

    @Test
    @DisplayName("getRevenueReport: tính toán báo cáo doanh thu & lợi nhuận thành công")
    void getRevenueReport_Success() {
        RevenueReportFilterRequest request = RevenueReportFilterRequest.builder()
                .from(LocalDate.of(2026, 10, 1))
                .to(LocalDate.of(2026, 10, 7))
                .groupBy("DAY")
                .build();

        when(orderRepository.findByStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                eq(OrderStatus.COMPLETED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(testOrder));
        when(orderRepository.findByStatusAndCompletedAtBetween(
                eq(OrderStatus.RETURNED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of());

        RevenueReportResponse response = adminReportService.getRevenueReport(request);

        assertThat(response.getSummary().getNetRevenue()).isEqualTo(300000L); // 320000 - 20000
        assertThat(response.getSummary().getCostOfGoods()).isEqualTo(180000L);
        assertThat(response.getSummary().getGrossProfit()).isEqualTo(120000L);
        assertThat(response.getSummary().getGrossMarginPercent()).isEqualTo(40.0);
        assertThat(response.getSummary().getCompletedOrders()).isEqualTo(1);
        assertThat(response.getSeries()).hasSize(7); // 7 days
    }

    @Test
    @DisplayName("getRevenueReport: ném lỗi INVALID_PARAMETER khi from > to")
    void getRevenueReport_InvalidDateRange() {
        RevenueReportFilterRequest request = RevenueReportFilterRequest.builder()
                .from(LocalDate.of(2026, 10, 10))
                .to(LocalDate.of(2026, 10, 5))
                .build();

        assertThatThrownBy(() -> adminReportService.getRevenueReport(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("getRevenueReport: ném lỗi DATE_RANGE_TOO_LARGE khi vượt quá 366 ngày")
    void getRevenueReport_DateRangeTooLarge() {
        RevenueReportFilterRequest request = RevenueReportFilterRequest.builder()
                .from(LocalDate.of(2025, 1, 1))
                .to(LocalDate.of(2026, 2, 10))
                .build();

        assertThatThrownBy(() -> adminReportService.getRevenueReport(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DATE_RANGE_TOO_LARGE);
    }

    @Test
    @DisplayName("getRevenueReport: ném lỗi INVALID_PARAMETER khi nhóm theo DAY vượt quá 92 ngày")
    void getRevenueReport_DayRangeTooLarge() {
        RevenueReportFilterRequest request = RevenueReportFilterRequest.builder()
                .from(LocalDate.of(2026, 1, 1))
                .to(LocalDate.of(2026, 5, 1))
                .groupBy("DAY")
                .build();

        assertThatThrownBy(() -> adminReportService.getRevenueReport(request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("getTopBooksReport: trả về bảng xếp hạng sách bán chạy thành công")
    void getTopBooksReport_Success() {
        TopBooksFilterRequest request = TopBooksFilterRequest.builder()
                .from(LocalDate.of(2026, 10, 1))
                .to(LocalDate.of(2026, 10, 7))
                .limit(10)
                .sortBy("QUANTITY")
                .build();

        when(orderRepository.findByStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                eq(OrderStatus.COMPLETED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(testOrder));

        List<TopBookReportResponse> response = adminReportService.getTopBooksReport(request);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getRank()).isEqualTo(1);
        assertThat(response.get(0).getBook().getTitle()).isEqualTo("Nhà Giả Kim");
        assertThat(response.get(0).getQuantitySold()).isEqualTo(3);
        assertThat(response.get(0).getRevenue()).isEqualTo(300000L);
    }

    @Test
    @DisplayName("getInventoryReport: trả về tổng quan và danh sách tồn kho thành công")
    void getInventoryReport_Success() {
        InventoryReportFilterRequest request = InventoryReportFilterRequest.builder().build();
        Pageable pageable = PageRequest.of(0, 20);

        when(bookRepository.count()).thenReturn(1L);
        when(bookRepository.findAll()).thenReturn(List.of(testBook));
        when(batchRepository.sumTotalStockValue()).thenReturn(3000000L);
        when(bookRepository.countByStockQuantityLessThanEqualAndStockQuantityGreaterThan(10, 0)).thenReturn(0L);
        when(bookRepository.countByStockQuantityLessThanEqual(0)).thenReturn(0L);
        when(orderRepository.findByStatusAndCompletedAtBetween(eq(OrderStatus.COMPLETED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(testOrder));

        Page<Book> bookPage = new PageImpl<>(List.of(testBook), pageable, 1);
        when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(bookPage);

        when(batchRepository.findAvgImportPriceByBookId(1L)).thenReturn(60000.0);
        when(batchRepository.sumStockValueByBookId(1L)).thenReturn(3000000L);

        InventoryReportResponse response = adminReportService.getInventoryReport(request, pageable);

        assertThat(response.getSummary().getTotalTitles()).isEqualTo(1L);
        assertThat(response.getSummary().getTotalStockValue()).isEqualTo(3000000L);
        assertThat(response.getItems().getItems()).hasSize(1);
        assertThat(response.getItems().getItems().get(0).getStockQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("getCustomerReport: trả về phân khúc và tỷ lệ mua lại thành công")
    void getCustomerReport_Success() {
        CustomerReportFilterRequest request = CustomerReportFilterRequest.builder()
                .from(LocalDate.of(2026, 10, 1))
                .to(LocalDate.of(2026, 10, 7))
                .limit(5)
                .build();

        Customer customer = Customer.builder()
                .id(1L)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .customerTier(CustomerTier.SILVER)
                .totalSpent(2500000L)
                .createdAt(LocalDate.of(2026, 10, 2).atStartOfDay().atOffset(VIETNAM_OFFSET))
                .build();

        when(customerRepository.countByCustomerTier(CustomerTier.BRONZE)).thenReturn(10L);
        when(customerRepository.countByCustomerTier(CustomerTier.SILVER)).thenReturn(5L);
        when(customerRepository.countByCustomerTier(CustomerTier.GOLD)).thenReturn(1L);

        when(customerRepository.findByCreatedAtBetween(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(customer));
        when(customerRepository.findByOrderByTotalSpentDesc(PageRequest.of(0, 5)))
                .thenReturn(List.of(customer));
        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(orderRepository.countByCustomerId(1L)).thenReturn(2L);

        CustomerReportResponse response = adminReportService.getCustomerReport(request);

        assertThat(response.getTierDistribution()).hasSize(3);
        assertThat(response.getNewCustomersSeries()).hasSize(7);
        assertThat(response.getTopCustomers()).hasSize(1);
        assertThat(response.getTopCustomers().get(0).getCustomer().getFullName()).isEqualTo("Nguyễn Tiến Chung");
        assertThat(response.getRepeatPurchaseRate()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("exportRevenueReport: xuất file CSV chứa BOM và header thành công")
    void exportRevenueReport_Success() {
        ExportReportRequest request = ExportReportRequest.builder()
                .from(LocalDate.of(2026, 10, 1))
                .to(LocalDate.of(2026, 10, 3))
                .groupBy("DAY")
                .format("CSV")
                .build();

        when(orderRepository.findByStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                eq(OrderStatus.COMPLETED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of(testOrder));
        when(orderRepository.findByStatusAndCompletedAtBetween(
                eq(OrderStatus.RETURNED), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(List.of());

        byte[] bytes = adminReportService.exportRevenueReport(request);

        assertThat(bytes).isNotEmpty();
        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertThat(csv).contains("Kỳ báo cáo");
        assertThat(csv).contains("Doanh thu thuần (VNĐ)");
    }
}

