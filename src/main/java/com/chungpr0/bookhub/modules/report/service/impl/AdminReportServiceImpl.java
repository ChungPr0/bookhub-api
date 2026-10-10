package com.chungpr0.bookhub.modules.report.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
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
import com.chungpr0.bookhub.modules.report.dto.response.InventoryReportItemResponse;
import com.chungpr0.bookhub.modules.report.dto.response.InventoryReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.RevenueReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.TopBookReportResponse;
import com.chungpr0.bookhub.modules.report.repository.specification.InventoryReportBookSpecification;
import com.chungpr0.bookhub.modules.report.service.AdminReportService;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private static final ZoneOffset VIETNAM_OFFSET = ZoneOffset.ofHours(7);
    private static final int MAX_EXPORT_ROWS = 50000;

    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final BatchRepository batchRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public RevenueReportResponse getRevenueReport(RevenueReportFilterRequest request) {
        if (request.getFrom().isAfter(request.getTo())) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }

        long days = ChronoUnit.DAYS.between(request.getFrom(), request.getTo());
        if (days > 366) {
            throw new AppException(ErrorCode.DATE_RANGE_TOO_LARGE);
        }

        String groupBy = (request.getGroupBy() != null && !request.getGroupBy().trim().isEmpty())
                ? request.getGroupBy().trim().toUpperCase()
                : "DAY";

        if ("DAY".equals(groupBy) && days > 92) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Vui lòng chọn nhóm theo TUẦN hoặc THÁNG khi khoảng thời gian lớn hơn 92 ngày");
        }

        OffsetDateTime fromDateTime = request.getFrom().atStartOfDay().atOffset(VIETNAM_OFFSET);
        OffsetDateTime toDateTime = request.getTo().atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);

        List<Order> rawCompletedOrders = orderRepository.findByStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                OrderStatus.COMPLETED,
                fromDateTime,
                toDateTime
        );

        // Apply category and paymentMethod filters
        List<Order> completedOrders = rawCompletedOrders.stream()
                .filter(o -> {
                    if (request.getPaymentMethod() != null && !request.getPaymentMethod().trim().isEmpty()) {
                        if (o.getPaymentMethod() == null || o.getPaymentMethod().getCode() == null
                                || !request.getPaymentMethod().trim().equalsIgnoreCase(o.getPaymentMethod().getCode().name())) {
                            return false;
                        }
                    }
                    if (request.getCategoryId() != null) {
                        if (o.getOrderDetails() == null) {
                            return false;
                        }
                        boolean hasCategory = o.getOrderDetails().stream().anyMatch(d -> {
                            if (d.getBook() == null || d.getBook().getCategory() == null) {
                                return false;
                            }
                            return request.getCategoryId().equals(d.getBook().getCategory().getId());
                        });
                        if (!hasCategory) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();

        List<Order> returnedOrders = orderRepository.findByStatusAndCompletedAtBetween(OrderStatus.RETURNED, fromDateTime, toDateTime);
        long returnedAmount = returnedOrders.stream().mapToLong(o -> o.getFinalAmount() != null ? o.getFinalAmount() : 0L).sum();

        long grossSales = completedOrders.stream().mapToLong(o -> o.getSubtotalAmount() != null ? o.getSubtotalAmount() : 0L).sum();
        long voucherDiscount = completedOrders.stream().mapToLong(o -> o.getVoucherDiscount() != null ? o.getVoucherDiscount() : 0L).sum();
        long pointsDiscount = completedOrders.stream().mapToLong(o -> o.getPointsDiscount() != null ? o.getPointsDiscount() : 0L).sum();
        long netRevenue = completedOrders.stream().mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L)).sum();

        long costOfGoods = completedOrders.stream()
                .filter(o -> o.getOrderDetails() != null)
                .flatMap(o -> o.getOrderDetails().stream())
                .mapToLong(d -> {
                    if (d.getCostAmount() != null) {
                        return d.getCostAmount();
                    }
                    return (long) ((d.getUnitPrice() != null ? d.getUnitPrice() : 0L) * 0.6 * (d.getQuantity() != null ? d.getQuantity() : 1));
                })
                .sum();

        long grossProfit = netRevenue - costOfGoods;
        Double grossMarginPercent = (netRevenue > 0)
                ? Math.round(((double) grossProfit / netRevenue * 100.0) * 10.0) / 10.0
                : 0.0;

        RevenueReportResponse.RevenueSummary summary = RevenueReportResponse.RevenueSummary.builder()
                .grossSales(grossSales)
                .voucherDiscount(voucherDiscount)
                .pointsDiscount(pointsDiscount)
                .netRevenue(netRevenue)
                .costOfGoods(costOfGoods)
                .grossProfit(grossProfit)
                .grossMarginPercent(grossMarginPercent)
                .completedOrders(completedOrders.size())
                .returnedOrders(returnedOrders.size())
                .returnedAmount(returnedAmount)
                .build();

        List<RevenueReportResponse.RevenueSeriesPoint> series = buildRevenueSeries(groupBy, request.getFrom(), request.getTo(), completedOrders);

        return RevenueReportResponse.builder()
                .summary(summary)
                .series(series)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopBookReportResponse> getTopBooksReport(TopBooksFilterRequest request) {
        LocalDate fromDate = (request != null && request.getFrom() != null)
                ? request.getFrom()
                : LocalDate.now(VIETNAM_OFFSET).minusDays(30);
        LocalDate toDate = (request != null && request.getTo() != null)
                ? request.getTo()
                : LocalDate.now(VIETNAM_OFFSET);

        int limit = (request != null && request.getLimit() != null)
                ? Math.min(100, Math.max(1, request.getLimit()))
                : 20;

        String sortBy = (request != null && request.getSortBy() != null)
                ? request.getSortBy().trim().toUpperCase()
                : "QUANTITY";

        OffsetDateTime fromDateTime = fromDate.atStartOfDay().atOffset(VIETNAM_OFFSET);
        OffsetDateTime toDateTime = toDate.atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);

        List<Order> completedOrders = orderRepository.findByStatusAndCompletedAtBetweenOrderByCompletedAtAsc(
                OrderStatus.COMPLETED,
                fromDateTime,
                toDateTime
        );

        Map<Long, TopBookAgg> map = new HashMap<>();

        for (Order order : completedOrders) {
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Book book = detail.getBook();
                    if (book == null) {
                        continue;
                    }

                    if (request != null && request.getCategoryId() != null) {
                        if (book.getCategory() == null || !request.getCategoryId().equals(book.getCategory().getId())) {
                            continue;
                        }
                    }

                    TopBookAgg agg = map.computeIfAbsent(book.getId(), k -> new TopBookAgg(book));
                    int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    long rev = detail.getLineTotal() != null ? detail.getLineTotal() : 0L;
                    long cost = detail.getCostAmount() != null
                            ? detail.getCostAmount()
                            : (long) ((detail.getUnitPrice() != null ? detail.getUnitPrice() : 0L) * 0.6 * qty);

                    agg.quantitySold += qty;
                    agg.revenue += rev;
                    agg.costOfGoods += cost;
                }
            }
        }

        Comparator<TopBookAgg> comparator;
        switch (sortBy) {
            case "REVENUE" -> comparator = (a, b) -> Long.compare(b.revenue, a.revenue);
            case "PROFIT" -> comparator = (a, b) -> Long.compare(b.getProfit(), a.getProfit());
            default -> comparator = (a, b) -> Integer.compare(b.quantitySold, a.quantitySold);
        }

        List<TopBookAgg> sortedList = map.values().stream()
                .sorted(comparator)
                .limit(limit)
                .toList();

        List<TopBookReportResponse> result = new ArrayList<>();
        int rank = 1;
        for (TopBookAgg agg : sortedList) {
            List<TopBookReportResponse.AuthorInfo> authors = (agg.book.getAuthors() != null)
                    ? agg.book.getAuthors().stream()
                    .map(a -> TopBookReportResponse.AuthorInfo.builder()
                            .id(a.getId())
                            .name(a.getName())
                            .build())
                    .toList()
                    : List.of();

            result.add(TopBookReportResponse.builder()
                    .rank(rank++)
                    .book(TopBookReportResponse.BookInfo.builder()
                            .id(agg.book.getId())
                            .isbn(agg.book.getIsbn())
                            .title(agg.book.getTitle())
                            .thumbnailUrl(agg.book.getThumbnailUrl())
                            .authors(authors)
                            .build())
                    .quantitySold(agg.quantitySold)
                    .revenue(agg.revenue)
                    .costOfGoods(agg.costOfGoods)
                    .profit(agg.getProfit())
                    .currentStock(agg.book.getStockQuantity())
                    .build());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryReportResponse getInventoryReport(InventoryReportFilterRequest request, Pageable pageable) {
        long totalTitles = bookRepository.count();
        List<Book> allBooks = bookRepository.findAll();
        long totalQuantity = allBooks.stream().mapToLong(b -> (long) b.getStockQuantity()).sum();
        long totalRetailValue = allBooks.stream().mapToLong(b -> (long) b.getStockQuantity() * (b.getSalePrice() != null ? b.getSalePrice() : 0L)).sum();

        Long totalStockValueDb = batchRepository.sumTotalStockValue();
        long totalStockValue = (totalStockValueDb != null && totalStockValueDb > 0)
                ? totalStockValueDb
                : (long) (totalRetailValue * 0.6);

        long lowStockCount = bookRepository.countByStockQuantityLessThanEqualAndStockQuantityGreaterThan(10, 0);
        long outOfStockCount = bookRepository.countByStockQuantityLessThanEqual(0);

        // Dead stock: books with stockQuantity > 0 and 0 sold in last 90 days
        OffsetDateTime ninetyDaysAgo = OffsetDateTime.now(VIETNAM_OFFSET).minusDays(90);
        List<Order> recentOrders = orderRepository.findByStatusAndCompletedAtBetween(
                OrderStatus.COMPLETED,
                ninetyDaysAgo,
                OffsetDateTime.now(VIETNAM_OFFSET)
        );
        Map<Long, Integer> recentSalesMap = new HashMap<>();
        for (Order o : recentOrders) {
            if (o.getOrderDetails() != null) {
                for (OrderDetail d : o.getOrderDetails()) {
                    if (d.getBook() != null) {
                        recentSalesMap.merge(d.getBook().getId(), d.getQuantity() != null ? d.getQuantity() : 0, (existing, additional) -> existing + additional);
                    }
                }
            }
        }

        long deadStockCount = allBooks.stream()
                .filter(b -> b.getStockQuantity() > 0 && recentSalesMap.getOrDefault(b.getId(), 0) == 0)
                .count();

        InventoryReportResponse.InventorySummary summary = InventoryReportResponse.InventorySummary.builder()
                .totalTitles(totalTitles)
                .totalQuantity(totalQuantity)
                .totalStockValue(totalStockValue)
                .totalRetailValue(totalRetailValue)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .deadStockCount(deadStockCount)
                .build();

        Page<Book> bookPage = bookRepository.findAll(InventoryReportBookSpecification.filter(request), pageable);

        List<InventoryReportItemResponse> items = bookPage.getContent().stream()
                .map(book -> {
                    int stockQuantity = book.getStockQuantity();
                    Long retailValue = (long) stockQuantity * (book.getSalePrice() != null ? book.getSalePrice() : 0L);

                    Double avgImportPriceDouble = batchRepository.findAvgImportPriceByBookId(book.getId());
                    long avgImportPrice = (avgImportPriceDouble != null)
                            ? avgImportPriceDouble.longValue()
                            : (long) ((book.getSalePrice() != null ? book.getSalePrice() : 0L) * 0.6);

                    Long stockValueDb = batchRepository.sumStockValueByBookId(book.getId());
                    long stockValue = (stockValueDb != null && stockValueDb > 0)
                            ? stockValueDb
                            : stockQuantity * avgImportPrice;

                    int soldLast90 = recentSalesMap.getOrDefault(book.getId(), 0);

                    return InventoryReportItemResponse.builder()
                            .book(InventoryReportItemResponse.SimpleBookInfo.builder()
                                    .id(book.getId())
                                    .isbn(book.getIsbn())
                                    .title(book.getTitle())
                                    .thumbnailUrl(book.getThumbnailUrl())
                                    .build())
                            .stockQuantity(stockQuantity)
                            .stockValue(stockValue)
                            .retailValue(retailValue)
                            .avgImportPrice(avgImportPrice)
                            .soldLast90Days(soldLast90)
                            .lastSoldAt(soldLast90 > 0 ? OffsetDateTime.now(VIETNAM_OFFSET) : null)
                            .build();
                })
                .toList();

        return InventoryReportResponse.builder()
                .summary(summary)
                .items(PageResponse.of(items, bookPage))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerReportResponse getCustomerReport(CustomerReportFilterRequest request) {
        LocalDate fromDate = (request != null && request.getFrom() != null)
                ? request.getFrom()
                : LocalDate.now(VIETNAM_OFFSET).minusDays(30);
        LocalDate toDate = (request != null && request.getTo() != null)
                ? request.getTo()
                : LocalDate.now(VIETNAM_OFFSET);

        int limit = (request != null && request.getLimit() != null)
                ? Math.min(50, Math.max(1, request.getLimit()))
                : 10;

        // 1. Tier distribution
        long bronzeCount = customerRepository.countByCustomerTier(CustomerTier.BRONZE);
        long silverCount = customerRepository.countByCustomerTier(CustomerTier.SILVER);
        long goldCount = customerRepository.countByCustomerTier(CustomerTier.GOLD);
        long totalCustomers = bronzeCount + silverCount + goldCount;

        List<CustomerReportResponse.TierDistributionItem> tierDistribution = List.of(
                CustomerReportResponse.TierDistributionItem.builder()
                        .tier(CustomerTier.BRONZE)
                        .count(bronzeCount)
                        .percent(totalCustomers > 0 ? Math.round(((double) bronzeCount / totalCustomers * 100.0) * 10.0) / 10.0 : 0.0)
                        .build(),
                CustomerReportResponse.TierDistributionItem.builder()
                        .tier(CustomerTier.SILVER)
                        .count(silverCount)
                        .percent(totalCustomers > 0 ? Math.round(((double) silverCount / totalCustomers * 100.0) * 10.0) / 10.0 : 0.0)
                        .build(),
                CustomerReportResponse.TierDistributionItem.builder()
                        .tier(CustomerTier.GOLD)
                        .count(goldCount)
                        .percent(totalCustomers > 0 ? Math.round(((double) goldCount / totalCustomers * 100.0) * 10.0) / 10.0 : 0.0)
                        .build()
        );

        // 2. New customers series
        OffsetDateTime fromDateTime = fromDate.atStartOfDay().atOffset(VIETNAM_OFFSET);
        OffsetDateTime toDateTime = toDate.atTime(23, 59, 59).atOffset(VIETNAM_OFFSET);
        List<Customer> registeredInRange = customerRepository.findByCreatedAtBetween(fromDateTime, toDateTime);

        List<CustomerReportResponse.NewCustomersSeriesItem> newCustomersSeries = new ArrayList<>();
        LocalDate curr = fromDate;
        DateTimeFormatter isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        while (!curr.isAfter(toDate)) {
            final LocalDate date = curr;
            long count = registeredInRange.stream()
                    .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().isEqual(date))
                    .count();
            newCustomersSeries.add(CustomerReportResponse.NewCustomersSeriesItem.builder()
                    .date(date.format(isoDateFormatter))
                    .count(count)
                    .build());
            curr = curr.plusDays(1);
        }

        // 3. Top customers
        List<Customer> topSpentCustomers = customerRepository.findByOrderByTotalSpentDesc(PageRequest.of(0, limit));
        List<CustomerReportResponse.TopCustomerItem> topCustomers = topSpentCustomers.stream()
                .map(c -> CustomerReportResponse.TopCustomerItem.builder()
                        .customer(CustomerReportResponse.SimpleCustomerInfo.builder()
                                .id(c.getId())
                                .fullName(c.getFullName())
                                .phone(c.getPhone())
                                .tier(c.getCustomerTier())
                                .build())
                        .orderCount(orderRepository.countByCustomerId(c.getId()))
                        .totalSpent(c.getTotalSpent())
                        .build())
                .toList();

        // 4. Repeat purchase rate
        List<Customer> allCust = customerRepository.findAll();
        long withOrders = 0;
        long repeatCust = 0;
        for (Customer c : allCust) {
            long count = orderRepository.countByCustomerId(c.getId());
            if (count >= 1) {
                withOrders++;
            }
            if (count >= 2) {
                repeatCust++;
            }
        }
        Double repeatPurchaseRate = (withOrders > 0)
                ? Math.round(((double) repeatCust / withOrders * 100.0) * 10.0) / 10.0
                : 0.0;

        return CustomerReportResponse.builder()
                .tierDistribution(tierDistribution)
                .newCustomersSeries(newCustomersSeries)
                .topCustomers(topCustomers)
                .repeatPurchaseRate(repeatPurchaseRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportRevenueReport(ExportReportRequest request) {
        RevenueReportFilterRequest filter = RevenueReportFilterRequest.builder()
                .from(request.getFrom())
                .to(request.getTo())
                .groupBy(request.getGroupBy())
                .build();

        RevenueReportResponse report = getRevenueReport(filter);

        if (report.getSeries().size() > MAX_EXPORT_ROWS) {
            throw new AppException(ErrorCode.EXPORT_TOO_LARGE);
        }

        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM
        sb.append('\uFEFF');
        sb.append("\"Kỳ báo cáo\",\"Doanh thu thuần (VNĐ)\",\"Giá vốn hàng bán (VNĐ)\",\"Lợi nhuận gộp (VNĐ)\",\"Số đơn hoàn tất\",\"Số lượng cuốn đã bán\"\n");

        for (RevenueReportResponse.RevenueSeriesPoint point : report.getSeries()) {
            sb.append("\"").append(point.getPeriodLabel()).append("\",");
            sb.append(point.getNetRevenue()).append(",");
            sb.append(point.getCostOfGoods()).append(",");
            sb.append(point.getGrossProfit()).append(",");
            sb.append(point.getOrderCount()).append(",");
            sb.append(point.getItemsSold()).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private List<RevenueReportResponse.RevenueSeriesPoint> buildRevenueSeries(
            String groupBy,
            LocalDate from,
            LocalDate to,
            List<Order> completedOrders
    ) {
        List<RevenueReportResponse.RevenueSeriesPoint> series = new ArrayList<>();

        if ("MONTH".equalsIgnoreCase(groupBy)) {
            LocalDate curr = from.withDayOfMonth(1);
            LocalDate end = to.withDayOfMonth(1);
            DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM/yyyy");
            DateTimeFormatter isoFmt = DateTimeFormatter.ofPattern("yyyy-MM");

            while (!curr.isAfter(end)) {
                final LocalDate monthStart = curr;
                final LocalDate monthEnd = curr.plusMonths(1).minusDays(1);

                List<Order> bucket = completedOrders.stream()
                        .filter(o -> {
                            LocalDate d = (o.getCompletedAt() != null ? o.getCompletedAt() : o.getCreatedAt()).toLocalDate();
                            return !d.isBefore(monthStart) && !d.isAfter(monthEnd);
                        })
                        .toList();

                series.add(createSeriesPoint(monthStart.format(isoFmt), monthStart.format(labelFmt), bucket));
                curr = curr.plusMonths(1);
            }
        } else if ("WEEK".equalsIgnoreCase(groupBy)) {
            LocalDate curr = from;
            DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (!curr.isAfter(to)) {
                final LocalDate weekStart = curr;
                final LocalDate weekEnd = curr.plusDays(6).isBefore(to) ? curr.plusDays(6) : to;

                List<Order> bucket = completedOrders.stream()
                        .filter(o -> {
                            LocalDate d = (o.getCompletedAt() != null ? o.getCompletedAt() : o.getCreatedAt()).toLocalDate();
                            return !d.isBefore(weekStart) && !d.isAfter(weekEnd);
                        })
                        .toList();

                String label = weekStart.format(labelFmt) + " - " + weekEnd.format(labelFmt);
                series.add(createSeriesPoint(weekStart.toString(), label, bucket));
                curr = curr.plusDays(7);
            }
        } else {
            // DAY
            LocalDate curr = from;
            DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (!curr.isAfter(to)) {
                final LocalDate day = curr;

                List<Order> bucket = completedOrders.stream()
                        .filter(o -> {
                            LocalDate d = (o.getCompletedAt() != null ? o.getCompletedAt() : o.getCreatedAt()).toLocalDate();
                            return d.isEqual(day);
                        })
                        .toList();

                series.add(createSeriesPoint(day.toString(), day.format(labelFmt), bucket));
                curr = curr.plusDays(1);
            }
        }

        return series;
    }

    private RevenueReportResponse.RevenueSeriesPoint createSeriesPoint(
            String periodStart,
            String periodLabel,
            List<Order> orders
    ) {
        long netRev = orders.stream()
                .mapToLong(o -> (o.getFinalAmount() != null ? o.getFinalAmount() : 0L) - (o.getShippingFee() != null ? o.getShippingFee() : 0L))
                .sum();

        long cost = orders.stream()
                .filter(o -> o.getOrderDetails() != null)
                .flatMap(o -> o.getOrderDetails().stream())
                .mapToLong(d -> d.getCostAmount() != null
                        ? d.getCostAmount()
                        : (long) ((d.getUnitPrice() != null ? d.getUnitPrice() : 0L) * 0.6 * (d.getQuantity() != null ? d.getQuantity() : 1)))
                .sum();

        long itemsSold = orders.stream()
                .filter(o -> o.getOrderDetails() != null)
                .flatMap(o -> o.getOrderDetails().stream())
                .mapToLong(d -> d.getQuantity() != null ? d.getQuantity() : 0)
                .sum();

        return RevenueReportResponse.RevenueSeriesPoint.builder()
                .periodStart(periodStart)
                .periodLabel(periodLabel)
                .netRevenue(netRev)
                .costOfGoods(cost)
                .grossProfit(netRev - cost)
                .orderCount(orders.size())
                .itemsSold(itemsSold)
                .build();
    }

    private static class TopBookAgg {
        Book book;
        int quantitySold = 0;
        long revenue = 0L;
        long costOfGoods = 0L;

        TopBookAgg(Book book) {
            this.book = book;
        }

        long getProfit() {
            return revenue - costOfGoods;
        }
    }
}
