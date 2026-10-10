package com.chungpr0.bookhub.modules.report.service.impl;

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
import com.chungpr0.bookhub.modules.report.service.AdminDashboardService;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.ofHours(7);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(String period) {
        String periodType = (period != null && !period.trim().isEmpty()) ? period.trim().toUpperCase() : "TODAY";

        LocalDate today = LocalDate.now(VIETNAM_OFFSET);
        OffsetDateTime from;
        OffsetDateTime to = today.atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);

        OffsetDateTime prevFrom;
        OffsetDateTime prevTo;

        switch (periodType) {
            case "LAST_7_DAYS" -> {
                from = today.minusDays(6).atStartOfDay().atOffset(VIETNAM_OFFSET);
                prevFrom = today.minusDays(13).atStartOfDay().atOffset(VIETNAM_OFFSET);
                prevTo = today.minusDays(7).atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);
            }
            case "LAST_30_DAYS" -> {
                from = today.minusDays(29).atStartOfDay().atOffset(VIETNAM_OFFSET);
                prevFrom = today.minusDays(59).atStartOfDay().atOffset(VIETNAM_OFFSET);
                prevTo = today.minusDays(30).atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);
            }
            case "THIS_MONTH" -> {
                from = today.withDayOfMonth(1).atStartOfDay().atOffset(VIETNAM_OFFSET);
                LocalDate firstOfLastMonth = today.minusMonths(1).withDayOfMonth(1);
                prevFrom = firstOfLastMonth.atStartOfDay().atOffset(VIETNAM_OFFSET);
                prevTo = firstOfLastMonth.plusMonths(1).minusDays(1).atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);
            }
            default -> {
                periodType = "TODAY";
                from = today.atStartOfDay().atOffset(VIETNAM_OFFSET);
                prevFrom = today.minusDays(1).atStartOfDay().atOffset(VIETNAM_OFFSET);
                prevTo = today.minusDays(1).atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);
            }
        }

        // 1. Revenue
        List<Order> completedOrders = orderRepository.findByStatusAndCompletedAtBetween(OrderStatus.COMPLETED, from, to);
        long netRevenue = completedOrders.stream()
                .mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L))
                .sum();

        List<Order> prevCompletedOrders = orderRepository.findByStatusAndCompletedAtBetween(OrderStatus.COMPLETED, prevFrom, prevTo);
        long prevNetRevenue = prevCompletedOrders.stream()
                .mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L))
                .sum();

        Double revenueChangePercent = (prevNetRevenue > 0)
                ? Math.round(((double) (netRevenue - prevNetRevenue) / prevNetRevenue * 100.0) * 10.0) / 10.0
                : 0.0;

        DashboardSummaryResponse.MetricChange revenueMetric = DashboardSummaryResponse.MetricChange.builder()
                .value(netRevenue)
                .previousValue(prevNetRevenue)
                .changePercent(revenueChangePercent)
                .build();

        // 2. Orders Metric
        List<Order> allOrders = orderRepository.findByCreatedAtBetween(from, to);
        long totalOrders = allOrders.size();

        List<Order> prevAllOrders = orderRepository.findByCreatedAtBetween(prevFrom, prevTo);
        long prevTotalOrders = prevAllOrders.size();

        Double ordersChangePercent = (prevTotalOrders > 0)
                ? Math.round(((double) (totalOrders - prevTotalOrders) / prevTotalOrders * 100.0) * 10.0) / 10.0
                : 0.0;

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus st : OrderStatus.values()) {
            long count = allOrders.stream().filter(o -> o.getStatus() == st).count();
            byStatus.put(st.name(), count);
        }

        DashboardSummaryResponse.OrdersMetric ordersMetric = DashboardSummaryResponse.OrdersMetric.builder()
                .total(totalOrders)
                .previousTotal(prevTotalOrders)
                .changePercent(ordersChangePercent)
                .byStatus(byStatus)
                .build();

        // 3. New Customers
        long newCustomers = customerRepository.countByCreatedAtBetween(from, to);
        long prevNewCustomers = customerRepository.countByCreatedAtBetween(prevFrom, prevTo);
        Double customersChangePercent = (prevNewCustomers > 0)
                ? Math.round(((double) (newCustomers - prevNewCustomers) / prevNewCustomers * 100.0) * 10.0) / 10.0
                : 0.0;

        DashboardSummaryResponse.MetricChange customerMetric = DashboardSummaryResponse.MetricChange.builder()
                .value(newCustomers)
                .previousValue(prevNewCustomers)
                .changePercent(customersChangePercent)
                .build();

        // 4. Avg Order Value & Items Sold
        Long avgOrderValue = (!completedOrders.isEmpty()) ? netRevenue / completedOrders.size() : 0L;

        long itemsSold = completedOrders.stream()
                .filter(o -> o.getOrderDetails() != null)
                .flatMap(o -> o.getOrderDetails().stream())
                .mapToLong(d -> d.getQuantity() != null ? d.getQuantity() : 0)
                .sum();

        // 5. Pending Actions
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        long unpaidBankTransfers = orderRepository.countByPaymentMethodCodeAndPaymentStatus(
                PaymentMethodCode.BANK_TRANSFER,
                PaymentStatus.UNPAID
        );
        long refundPending = orderRepository.countByPaymentStatus(PaymentStatus.REFUND_PENDING);
        long lowStockBooks = bookRepository.countByStockQuantityLessThanEqualAndStockQuantityGreaterThan(10, 0);
        long outOfStockBooks = bookRepository.countByStockQuantityLessThanEqual(0);
        long reviewsWithoutReply = reviewRepository.countByAdminReplyIsNull();

        DashboardSummaryResponse.PendingActions pendingActions = DashboardSummaryResponse.PendingActions.builder()
                .pendingOrders(pendingOrders)
                .unpaidBankTransfers(unpaidBankTransfers)
                .refundPending(refundPending)
                .lowStockBooks(lowStockBooks)
                .outOfStockBooks(outOfStockBooks)
                .reviewsWithoutReply(reviewsWithoutReply)
                .build();

        // 6. Revenue Chart
        List<DashboardSummaryResponse.ChartPoint> revenueChart = buildRevenueChart(periodType, completedOrders, from, to);

        // 7. Top Books
        List<DashboardSummaryResponse.TopBookItem> topBooks = buildTopBooks(completedOrders);

        return DashboardSummaryResponse.builder()
                .period(DashboardSummaryResponse.PeriodInfo.builder()
                        .type(periodType)
                        .from(from)
                        .to(to)
                        .build())
                .revenue(revenueMetric)
                .orders(ordersMetric)
                .newCustomers(customerMetric)
                .avgOrderValue(avgOrderValue)
                .itemsSold(itemsSold)
                .pendingActions(pendingActions)
                .revenueChart(revenueChart)
                .topBooks(topBooks)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardRealtimeResponse getDashboardRealtime() {
        OffsetDateTime now = OffsetDateTime.now(VIETNAM_OFFSET);
        LocalDate today = now.toLocalDate();
        OffsetDateTime todayStart = today.atStartOfDay().atOffset(VIETNAM_OFFSET);
        OffsetDateTime todayEnd = today.atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);

        List<Order> ordersToday = orderRepository.findByCreatedAtBetween(todayStart, todayEnd);

        List<Order> completedToday = ordersToday.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .toList();
        List<Order> cancelledToday = ordersToday.stream()
                .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
                .toList();

        long grossRevenue = completedToday.stream().mapToLong(o -> o.getFinalAmount() != null ? o.getFinalAmount() : 0L).sum();
        long netRevenue = completedToday.stream()
                .mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L))
                .sum();

        long todayOrdersCount = ordersToday.size();
        long todayCompletedCount = completedToday.size();
        long todayCancelledCount = cancelledToday.size();

        Double cancellationRate = (todayOrdersCount > 0)
                ? Math.round(((double) todayCancelledCount / todayOrdersCount * 100.0) * 100.0) / 100.0
                : 0.0;

        DashboardRealtimeResponse.TodaySummary todaySummary = DashboardRealtimeResponse.TodaySummary.builder()
                .todayGrossRevenue(grossRevenue)
                .todayNetRevenue(netRevenue)
                .todayOrdersCount(todayOrdersCount)
                .todayCompletedCount(todayCompletedCount)
                .todayCancelledCount(todayCancelledCount)
                .cancellationRate(cancellationRate)
                .averageProcessingTimeMinutes(35)
                .build();

        // Hourly breakdown (from 0 to 23)
        List<DashboardRealtimeResponse.HourlyBreakdown> hourlyBreakdown = new ArrayList<>();
        int currentHour = now.getHour();
        for (int h = 0; h <= currentHour; h++) {
            final int hour = h;
            List<Order> hourOrders = ordersToday.stream()
                    .filter(o -> o.getCreatedAt().getHour() == hour)
                    .toList();
            long hourRevenue = hourOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L))
                    .sum();

            hourlyBreakdown.add(DashboardRealtimeResponse.HourlyBreakdown.builder()
                    .hour(hour)
                    .orders(hourOrders.size())
                    .revenue(hourRevenue)
                    .build());
        }

        DashboardRealtimeResponse.UrgentQueues urgentQueues = DashboardRealtimeResponse.UrgentQueues.builder()
                .unconfirmedOrdersCount(orderRepository.countByStatus(OrderStatus.PENDING))
                .pendingShipmentCount(orderRepository.countByStatus(OrderStatus.CONFIRMED))
                .unpaidBankTransferCount(orderRepository.countByPaymentMethodCodeAndPaymentStatus(
                        PaymentMethodCode.BANK_TRANSFER,
                        PaymentStatus.UNPAID
                ))
                .returnRequestsCount(orderRepository.countByStatus(OrderStatus.RETURNED))
                .build();

        return DashboardRealtimeResponse.builder()
                .asOfTime(now)
                .todaySummary(todaySummary)
                .hourlyBreakdown(hourlyBreakdown)
                .urgentQueues(urgentQueues)
                .build();
    }

    private List<DashboardSummaryResponse.ChartPoint> buildRevenueChart(
            String periodType,
            List<Order> completedOrders,
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        List<DashboardSummaryResponse.ChartPoint> points = new ArrayList<>();

        if ("TODAY".equals(periodType)) {
            // Group by 4-hour intervals: 00:00, 04:00, 08:00, 12:00, 16:00, 20:00
            for (int h = 0; h < 24; h += 4) {
                final int startH = h;
                final int endH = h + 4;
                String label = String.format("%02d:00", startH);

                List<Order> bucketOrders = completedOrders.stream()
                        .filter(o -> {
                            OffsetDateTime dt = o.getCompletedAt() != null ? o.getCompletedAt() : o.getCreatedAt();
                            int hour = dt.getHour();
                            return hour >= startH && hour < endH;
                        })
                        .toList();

                long rev = bucketOrders.stream()
                        .mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L))
                        .sum();

                points.add(DashboardSummaryResponse.ChartPoint.builder()
                        .timeLabel(label)
                        .revenue(rev)
                        .orderCount(bucketOrders.size())
                        .build());
            }
        } else {
            // Group by day
            LocalDate curr = from.toLocalDate();
            LocalDate end = to.toLocalDate();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

            while (!curr.isAfter(end)) {
                final LocalDate date = curr;
                String label = date.format(formatter);

                List<Order> bucketOrders = completedOrders.stream()
                        .filter(o -> {
                            OffsetDateTime dt = o.getCompletedAt() != null ? o.getCompletedAt() : o.getCreatedAt();
                            return dt.toLocalDate().isEqual(date);
                        })
                        .toList();

                long rev = bucketOrders.stream()
                        .mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L))
                        .sum();

                points.add(DashboardSummaryResponse.ChartPoint.builder()
                        .timeLabel(label)
                        .revenue(rev)
                        .orderCount(bucketOrders.size())
                        .build());

                curr = curr.plusDays(1);
            }
        }

        return points;
    }

    private List<DashboardSummaryResponse.TopBookItem> buildTopBooks(List<Order> completedOrders) {
        Map<Long, BookAgg> map = new HashMap<>();

        for (Order order : completedOrders) {
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    if (detail.getBook() != null) {
                        Long bookId = detail.getBook().getId();
                        BookAgg agg = map.computeIfAbsent(bookId, k -> new BookAgg(
                                detail.getBook().getId(),
                                detail.getBook().getTitle(),
                                detail.getBook().getSlug(),
                                detail.getBook().getThumbnailUrl()
                        ));
                        agg.quantity += (detail.getQuantity() != null ? detail.getQuantity() : 0);
                        agg.revenue += (detail.getLineTotal() != null ? detail.getLineTotal() : 0L);
                    }
                }
            }
        }

        return map.values().stream()
                .sorted((a, b) -> Integer.compare(b.quantity, a.quantity))
                .limit(5)
                .map(agg -> DashboardSummaryResponse.TopBookItem.builder()
                        .book(DashboardSummaryResponse.SimpleBookInfo.builder()
                                .id(agg.id)
                                .title(agg.title)
                                .slug(agg.slug)
                                .thumbnailUrl(agg.thumbnailUrl)
                                .build())
                        .quantitySold(agg.quantity)
                        .revenue(agg.revenue)
                        .build())
                .toList();
    }

    private static class BookAgg {
        Long id;
        String title;
        String slug;
        String thumbnailUrl;
        int quantity = 0;
        long revenue = 0L;

        BookAgg(Long id, String title, String slug, String thumbnailUrl) {
            this.id = id;
            this.title = title;
            this.slug = slug;
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}

