package com.chungpr0.bookhub.modules.catalog.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewReplyRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewVisibilityRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminReviewResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSummaryItemResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.CustomerReviewItemResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PendingReviewItemResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.ReviewAdminReplyResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.ReviewCustomerInfoResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Review;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.specification.AdminReviewSpecification;
import com.chungpr0.bookhub.modules.catalog.service.ReviewService;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PendingReviewItemResponse> getPendingReviews(Long accountId, int page, int size) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);

        List<Order> orders = orderRepository.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("customer").get("id"), customer.getId()),
                cb.equal(root.get("status"), OrderStatus.COMPLETED)
        ));

        List<PendingReviewItemResponse> allPending = new ArrayList<>();
        for (Order order : orders) {
            OffsetDateTime completionDate = order.getCompletedAt() != null ? order.getCompletedAt() : order.getUpdatedAt();
            if (completionDate == null || completionDate.isBefore(thirtyDaysAgo)) {
                continue;
            }

            OffsetDateTime deadline = completionDate.plusDays(30);
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Long bookId = detail.getBook().getId();
                    if (!reviewRepository.existsByOrderIdAndBookId(order.getId(), bookId)) {
                        Book book = detail.getBook();
                        BookSummaryItemResponse bookSummary = BookSummaryItemResponse.builder()
                                .id(book.getId())
                                .title(book.getTitle())
                                .slug(book.getSlug())
                                .thumbnailUrl(book.getThumbnailUrl())
                                .build();

                        allPending.add(PendingReviewItemResponse.builder()
                                .orderId(order.getId())
                                .orderCode(order.getOrderCode())
                                .completedAt(completionDate)
                                .reviewDeadline(deadline)
                                .book(bookSummary)
                                .build());
                    }
                }
            }
        }

        int pageSize = Math.min(Math.max(size, 1), 50);
        int totalElements = allPending.size();
        int fromIndex = Math.min(page * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List<PendingReviewItemResponse> pagedItems = allPending.subList(fromIndex, toIndex);
        return PageResponse.of(pagedItems, page, pageSize, totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CustomerReviewItemResponse> getCustomerReviews(Long accountId, int page, int size) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId(), pageable);

        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);

        List<CustomerReviewItemResponse> items = reviewPage.getContent().stream()
                .map(r -> {
                    Order order = orderRepository.findById(r.getOrderId()).orElse(null);
                    String orderCode = order != null ? order.getOrderCode() : null;

                    Book book = r.getBook();
                    BookSummaryItemResponse bookSummary = BookSummaryItemResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .slug(book.getSlug())
                            .thumbnailUrl(book.getThumbnailUrl())
                            .build();

                    ReviewAdminReplyResponse adminReply = r.getAdminReply() != null
                            ? ReviewAdminReplyResponse.builder()
                            .content(r.getAdminReply())
                            .repliedAt(r.getRepliedAt())
                            .build()
                            : null;

                    boolean canEdit = r.getStatus() != ReviewStatus.HIDDEN && r.getCreatedAt().isAfter(thirtyDaysAgo);
                    OffsetDateTime editDeadline = r.getCreatedAt().plusDays(30);

                    return CustomerReviewItemResponse.builder()
                            .id(r.getId())
                            .orderCode(orderCode)
                            .book(bookSummary)
                            .rating(r.getRating())
                            .content(r.getContent())
                            .status(r.getStatus())
                            .hiddenReason(r.getHiddenReason())
                            .canEdit(canEdit)
                            .editDeadline(editDeadline)
                            .adminReply(adminReply)
                            .createdAt(r.getCreatedAt())
                            .updatedAt(r.getUpdatedAt())
                            .build();
                })
                .toList();

        return PageResponse.of(items, reviewPage);
    }

    @Override
    @Transactional
    public CustomerReviewItemResponse createReview(Long accountId, CreateReviewRequest request) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Order order = orderRepository.findByOrderCodeAndCustomerId(request.getOrderCode(), customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND, "Không tìm thấy đơn hàng yêu cầu"));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED, "Chỉ được phép đánh giá sản phẩm từ đơn hàng đã hoàn tất");
        }

        boolean bookInOrder = order.getOrderDetails() != null && order.getOrderDetails().stream()
                .anyMatch(d -> d.getBook().getId().equals(request.getBookId()));
        if (!bookInOrder) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED, "Cuốn sách không thuộc đơn hàng đã hoàn tất");
        }

        OffsetDateTime completedAt = order.getCompletedAt() != null ? order.getCompletedAt() : order.getUpdatedAt();
        if (completedAt != null && completedAt.isBefore(OffsetDateTime.now().minusDays(30))) {
            throw new AppException(ErrorCode.REVIEW_PERIOD_EXPIRED, "Đã hết thời hạn 30 ngày để gửi đánh giá cho đơn hàng này");
        }

        if (reviewRepository.existsByOrderIdAndBookId(order.getId(), request.getBookId())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS, "Bạn đã gửi đánh giá cho cuốn sách này trong đơn hàng trước đó");
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        Review review = Review.builder()
                .customer(customer)
                .orderId(order.getId())
                .book(book)
                .rating(request.getRating())
                .content(request.getContent())
                .status(ReviewStatus.VISIBLE)
                .build();

        Review saved = reviewRepository.save(review);
        recalculateBookReviewStats(book.getId());

        BookSummaryItemResponse bookSummary = BookSummaryItemResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .thumbnailUrl(book.getThumbnailUrl())
                .build();

        return CustomerReviewItemResponse.builder()
                .id(saved.getId())
                .orderCode(order.getOrderCode())
                .book(bookSummary)
                .rating(saved.getRating())
                .content(saved.getContent())
                .status(saved.getStatus())
                .canEdit(true)
                .editDeadline(saved.getCreatedAt().plusDays(30))
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public CustomerReviewItemResponse updateReview(Long accountId, Long reviewId, UpdateReviewRequest request) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Review review = reviewRepository.findByIdAndCustomerId(reviewId, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND, "Không tìm thấy bài đánh giá yêu cầu"));

        if (review.getStatus() == ReviewStatus.HIDDEN) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED, "Bài đánh giá đang bị ẩn bởi quản trị viên, không thể chỉnh sửa");
        }

        if (review.getCreatedAt().isBefore(OffsetDateTime.now().minusDays(30))) {
            throw new AppException(ErrorCode.REVIEW_PERIOD_EXPIRED, "Đã quá hạn 30 ngày cho phép chỉnh sửa bài đánh giá");
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());

        Review updated = reviewRepository.save(review);
        recalculateBookReviewStats(updated.getBook().getId());

        Order order = orderRepository.findById(updated.getOrderId()).orElse(null);
        String orderCode = order != null ? order.getOrderCode() : null;

        Book book = updated.getBook();
        BookSummaryItemResponse bookSummary = BookSummaryItemResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .slug(book.getSlug())
                .thumbnailUrl(book.getThumbnailUrl())
                .build();

        ReviewAdminReplyResponse adminReply = updated.getAdminReply() != null
                ? ReviewAdminReplyResponse.builder()
                .content(updated.getAdminReply())
                .repliedAt(updated.getRepliedAt())
                .build()
                : null;

        return CustomerReviewItemResponse.builder()
                .id(updated.getId())
                .orderCode(orderCode)
                .book(bookSummary)
                .rating(updated.getRating())
                .content(updated.getContent())
                .status(updated.getStatus())
                .hiddenReason(updated.getHiddenReason())
                .canEdit(true)
                .editDeadline(updated.getCreatedAt().plusDays(30))
                .adminReply(adminReply)
                .createdAt(updated.getCreatedAt())
                .updatedAt(updated.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteReview(Long accountId, Long reviewId) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        Review review = reviewRepository.findByIdAndCustomerId(reviewId, customer.getId())
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND, "Không tìm thấy bài đánh giá yêu cầu"));

        if (review.getCreatedAt().isBefore(OffsetDateTime.now().minusDays(30))) {
            throw new AppException(ErrorCode.REVIEW_PERIOD_EXPIRED, "Đã quá hạn 30 ngày cho phép xóa bài đánh giá");
        }

        Long bookId = review.getBook().getId();
        reviewRepository.delete(review);
        recalculateBookReviewStats(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminReviewResponse> getAdminReviews(String keyword, Long bookId, Integer rating, ReviewStatus status, Boolean hasReply, int page, int size, String sort) {
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(size, 1), 100), sortObj);

        Specification<Review> spec = AdminReviewSpecification.filter(keyword, bookId, rating, status, hasReply);
        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);

        List<AdminReviewResponse> items = reviewPage.getContent().stream()
                .map(r -> {
                    Order order = orderRepository.findById(r.getOrderId()).orElse(null);
                    String orderCode = order != null ? order.getOrderCode() : null;

                    Customer cust = r.getCustomer();
                    ReviewCustomerInfoResponse customerInfo = ReviewCustomerInfoResponse.builder()
                            .id(cust.getId())
                            .fullName(cust.getFullName())
                            .phone(cust.getPhone())
                            .avatarUrl(cust.getAvatarUrl())
                            .build();

                    Book book = r.getBook();
                    BookSummaryItemResponse bookSummary = BookSummaryItemResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .slug(book.getSlug())
                            .thumbnailUrl(book.getThumbnailUrl())
                            .build();

                    ReviewAdminReplyResponse adminReply = r.getAdminReply() != null
                            ? ReviewAdminReplyResponse.builder()
                            .content(r.getAdminReply())
                            .repliedAt(r.getRepliedAt())
                            .build()
                            : null;

                    return AdminReviewResponse.builder()
                            .id(r.getId())
                            .orderId(r.getOrderId())
                            .orderCode(orderCode)
                            .customer(customerInfo)
                            .book(bookSummary)
                            .rating(r.getRating())
                            .content(r.getContent())
                            .status(r.getStatus())
                            .hiddenReason(r.getHiddenReason())
                            .adminReply(adminReply)
                            .createdAt(r.getCreatedAt())
                            .updatedAt(r.getUpdatedAt())
                            .build();
                })
                .toList();

        return PageResponse.of(items, reviewPage);
    }

    @Override
    @Transactional
    public void updateReviewVisibility(Long reviewId, AdminReviewVisibilityRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND, "Không tìm thấy bài đánh giá"));

        if (request.getStatus() == ReviewStatus.HIDDEN && !StringUtils.hasText(request.getReason())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Vui lòng nhập lý do ẩn đánh giá");
        }

        review.setStatus(request.getStatus());
        review.setHiddenReason(request.getStatus() == ReviewStatus.HIDDEN ? request.getReason() : null);
        reviewRepository.save(review);

        recalculateBookReviewStats(review.getBook().getId());
    }

    @Override
    @Transactional
    public void replyReview(Long reviewId, AdminReviewReplyRequest request, Long staffAccountId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND, "Không tìm thấy bài đánh giá"));

        review.setAdminReply(request.getContent().trim());
        review.setRepliedBy(staffAccountId);
        review.setRepliedAt(OffsetDateTime.now());

        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReviewReply(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND, "Không tìm thấy bài đánh giá"));

        review.setAdminReply(null);
        review.setRepliedBy(null);
        review.setRepliedAt(null);

        reviewRepository.save(review);
    }

    private void recalculateBookReviewStats(Long bookId) {
        long visibleCount = reviewRepository.countVisibleReviewsByBookId(bookId);
        Double avg = reviewRepository.getAverageVisibleRatingByBookId(bookId);
        double avgRating = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null) {
            book.setReviewCount((int) visibleCount);
            book.setAvgRating(avgRating);
            bookRepository.save(book);
        }
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, property);
    }
}

