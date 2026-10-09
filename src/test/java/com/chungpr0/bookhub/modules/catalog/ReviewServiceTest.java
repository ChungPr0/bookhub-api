package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewReplyRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.AdminReviewVisibilityRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateReviewRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.CustomerReviewItemResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PendingReviewItemResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Review;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.ReviewServiceImpl;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Customer customer;
    private Book book;
    private Order order;
    private OrderDetail orderDetail;
    private Review review;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .fullName("Nguyễn Văn An")
                .customerTier(CustomerTier.BRONZE)
                .build();

        book = Book.builder()
                .id(10L)
                .title("Đắc Nhân Tâm")
                .slug("dac-nhan-tam")
                .avgRating(0.0)
                .reviewCount(0)
                .images(new ArrayList<>())
                .build();

        orderDetail = OrderDetail.builder()
                .id(100L)
                .book(book)
                .bookTitle("Đắc Nhân Tâm")
                .quantity(1)
                .unitPrice(80000L)
                .lineTotal(80000L)
                .build();

        order = Order.builder()
                .id(50L)
                .orderCode("ORD-12345")
                .customer(customer)
                .status(OrderStatus.COMPLETED)
                .completedAt(OffsetDateTime.now().minusDays(5))
                .orderDetails(List.of(orderDetail))
                .build();

        review = Review.builder()
                .id(200L)
                .book(book)
                .customer(customer)
                .orderId(50L)
                .rating(5)
                .content("Sách rất hay và bổ ích!")
                .status(ReviewStatus.VISIBLE)
                .createdAt(OffsetDateTime.now().minusDays(2))
                .build();
    }

    @Test
    @DisplayName("getPendingReviews - Thành công trả về danh sách sách đã mua chưa đánh giá")
    void testGetPendingReviews_Success() {
        when(customerRepository.findByAccountId(10L)).thenReturn(Optional.of(customer));
        when(orderRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Order>>any()))
                .thenReturn(List.of(order));
        when(reviewRepository.existsByOrderIdAndBookId(50L, 10L)).thenReturn(false);

        PageResponse<PendingReviewItemResponse> response = reviewService.getPendingReviews(10L, 0, 10);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getBook().getId()).isEqualTo(10L);
        assertThat(response.getItems().get(0).getOrderId()).isEqualTo(50L);
    }

    @Test
    @DisplayName("createReview - Thành công tạo đánh giá và tính lại avgRating")
    void testCreateReview_Success() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookId(10L)
                .orderCode("ORD-12345")
                .rating(5)
                .content("Tuyệt phẩm kinh điển, rất đáng đọc!")
                .build();

        when(customerRepository.findByAccountId(10L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-12345", 1L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderIdAndBookId(50L, 10L)).thenReturn(false);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(201L);
            r.setCreatedAt(OffsetDateTime.now());
            r.setUpdatedAt(OffsetDateTime.now());
            return r;
        });
        when(reviewRepository.countVisibleReviewsByBookId(10L)).thenReturn(1L);
        when(reviewRepository.getAverageVisibleRatingByBookId(10L)).thenReturn(5.0);

        CustomerReviewItemResponse response = reviewService.createReview(10L, request);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(book.getAvgRating()).isEqualTo(5.0);
        assertThat(book.getReviewCount()).isEqualTo(1);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("createReview - Ném lỗi REVIEW_NOT_ALLOWED khi đơn hàng chưa hoàn tất")
    void testCreateReview_OrderNotCompleted() {
        order.setStatus(OrderStatus.CONFIRMED);
        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookId(10L)
                .orderCode("ORD-12345")
                .rating(5)
                .content("Bình luận")
                .build();

        when(customerRepository.findByAccountId(10L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-12345", 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.REVIEW_NOT_ALLOWED));
    }

    @Test
    @DisplayName("createReview - Ném lỗi REVIEW_PERIOD_EXPIRED khi đơn hàng đã hoàn tất quá 30 ngày")
    void testCreateReview_ExpiredPeriod() {
        order.setCompletedAt(OffsetDateTime.now().minusDays(35));
        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookId(10L)
                .orderCode("ORD-12345")
                .rating(5)
                .content("Bình luận")
                .build();

        when(customerRepository.findByAccountId(10L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-12345", 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.REVIEW_PERIOD_EXPIRED));
    }

    @Test
    @DisplayName("createReview - Ném lỗi REVIEW_ALREADY_EXISTS khi khách đã đánh giá cuốn sách trong đơn này")
    void testCreateReview_AlreadyReviewed() {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .bookId(10L)
                .orderCode("ORD-12345")
                .rating(5)
                .content("Bình luận")
                .build();

        when(customerRepository.findByAccountId(10L)).thenReturn(Optional.of(customer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-12345", 1L)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrderIdAndBookId(50L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(10L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("updateReview - Thành công cập nhật và tính lại avgRating")
    void testUpdateCustomerReview_Success() {
        UpdateReviewRequest request = UpdateReviewRequest.builder()
                .rating(4)
                .content("Sách rất hay, tuy nhiên bìa hơi cong nhẹ do vận chuyển")
                .build();

        when(customerRepository.findByAccountId(10L)).thenReturn(Optional.of(customer));
        when(reviewRepository.findByIdAndCustomerId(200L, 1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reviewRepository.countVisibleReviewsByBookId(10L)).thenReturn(1L);
        when(reviewRepository.getAverageVisibleRatingByBookId(10L)).thenReturn(4.0);

        CustomerReviewItemResponse response = reviewService.updateReview(10L, 200L, request);

        assertThat(response).isNotNull();
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(book.getAvgRating()).isEqualTo(4.0);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("updateReviewVisibility - Thành công ẩn đánh giá vi phạm và loại khỏi avgRating")
    void testUpdateReviewVisibility_Hide() {
        AdminReviewVisibilityRequest request = AdminReviewVisibilityRequest.builder()
                .status(ReviewStatus.HIDDEN)
                .reason("Ngôn từ không phù hợp")
                .build();

        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reviewRepository.countVisibleReviewsByBookId(10L)).thenReturn(0L);
        when(reviewRepository.getAverageVisibleRatingByBookId(10L)).thenReturn(null);

        reviewService.updateReviewVisibility(200L, request);

        assertThat(review.getStatus()).isEqualTo(ReviewStatus.HIDDEN);
        assertThat(review.getHiddenReason()).isEqualTo("Ngôn từ không phù hợp");
        assertThat(book.getAvgRating()).isEqualTo(0.0);
        assertThat(book.getReviewCount()).isEqualTo(0);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("replyReview - Thành công phản hồi đánh giá của khách hàng")
    void testReplyReview_Success() {
        AdminReviewReplyRequest request = AdminReviewReplyRequest.builder()
                .content("Cảm ơn quý khách đã ủng hộ BookHub!")
                .build();

        when(reviewRepository.findById(200L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.replyReview(200L, request, 999L);

        assertThat(review.getAdminReply()).isEqualTo("Cảm ơn quý khách đã ủng hộ BookHub!");
        assertThat(review.getRepliedBy()).isEqualTo(999L);
        assertThat(review.getRepliedAt()).isNotNull();
    }
}
