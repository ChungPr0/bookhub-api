package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.ReviewStatus;
import com.chungpr0.bookhub.common.enums.StockStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.catalog.dto.request.BookSearchFilter;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookReviewResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSearchResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.BookSuggestResponse;
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
import com.chungpr0.bookhub.modules.catalog.service.impl.BookServiceImpl;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private Category testCategory;
    private Author testAuthor;
    private Publisher testPublisher;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(5L)
                .name("Tiểu thuyết")
                .slug("tieu-thuyet")
                .build();

        testAuthor = Author.builder()
                .id(7L)
                .name("Paulo Coelho")
                .slug("paulo-coelho")
                .build();

        testPublisher = Publisher.builder()
                .id(4L)
                .name("NXB Hội Nhà Văn")
                .slug("nxb-hoi-nha-van")
                .build();

        testBook = Book.builder()
                .id(101L)
                .isbn("9786045629870")
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .category(testCategory)
                .publisher(testPublisher)
                .authors(List.of(testAuthor))
                .originalPrice(79000L)
                .salePrice(63200L)
                .stockQuantity(84)
                .lowStockThreshold(10)
                .soldCount(5320)
                .avgRating(4.7)
                .reviewCount(1284)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .createdAt(OffsetDateTime.now())
                .images(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("searchBooks - Thành công trả về danh sách sách, phân trang và facets")
    void testSearchBooks_Success() {
        when(bookRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testBook)));

        BookSearchFilter filter = BookSearchFilter.builder().keyword("Nhà Giả Kim").build();
        BookSearchResponse response = bookService.searchBooks(filter, 0, 20, "relevance");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getTitle()).isEqualTo("Nhà Giả Kim");
        assertThat(response.getItems().get(0).getStockStatus()).isEqualTo(StockStatus.IN_STOCK);
        assertThat(response.getPage().getTotalElements()).isEqualTo(1);
        assertThat(response.getFacets().getLanguages()).isNotEmpty();
    }

    @Test
    @DisplayName("searchBooks - Thất bại khi kích thước trang size > 60")
    void testSearchBooks_InvalidSize() {
        BookSearchFilter filter = BookSearchFilter.builder().build();
        assertThatThrownBy(() -> bookService.searchBooks(filter, 0, 100, null))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAMETER));
    }

    @Test
    @DisplayName("searchBooks - Thất bại khi khoảng giá priceFrom > priceTo")
    void testSearchBooks_InvalidPriceRange() {
        BookSearchFilter filter = BookSearchFilter.builder().priceFrom(200000L).priceTo(100000L).build();
        assertThatThrownBy(() -> bookService.searchBooks(filter, 0, 20, null))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAMETER));
    }

    @Test
    @DisplayName("searchBooks - Thất bại khi sort không thuộc whitelist")
    void testSearchBooks_InvalidSort() {
        BookSearchFilter filter = BookSearchFilter.builder().build();
        assertThatThrownBy(() -> bookService.searchBooks(filter, 0, 20, "sql_injection;drop table"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAMETER));
    }

    @Test
    @DisplayName("getBestSellers - Thành công trả về danh sách sách bán chạy")
    void testGetBestSellers_Success() {
        when(bookRepository.findTopBestSellers(any(Pageable.class)))
                .thenReturn(List.of(testBook));

        List<BookCardResponse> bestSellers = bookService.getBestSellers(10, null);

        assertThat(bestSellers).hasSize(1);
        assertThat(bestSellers.get(0).getSoldCount()).isEqualTo(5320);
        assertThat(bestSellers.get(0).isBestSeller()).isTrue();
    }

    @Test
    @DisplayName("getNewArrivals - Thành công trả về danh sách sách mới")
    void testGetNewArrivals_Success() {
        when(bookRepository.findNewArrivals(any(Pageable.class)))
                .thenReturn(List.of(testBook));

        List<BookCardResponse> newArrivals = bookService.getNewArrivals(10, null);

        assertThat(newArrivals).hasSize(1);
        assertThat(newArrivals.get(0).getTitle()).isEqualTo("Nhà Giả Kim");
    }

    @Test
    @DisplayName("suggest - Thành công trả về gợi ý tìm kiếm")
    void testSuggest_Success() {
        when(bookRepository.searchAutocompleteBooks(eq("Nhà"), any(Pageable.class)))
                .thenReturn(List.of(testBook));
        when(authorRepository.findTop10ByNameContainingIgnoreCase("Nhà")).thenReturn(Collections.emptyList());
        when(categoryRepository.findTop10ByNameContainingIgnoreCase("Nhà")).thenReturn(Collections.emptyList());

        BookSuggestResponse response = bookService.suggest("Nhà");

        assertThat(response.getBooks()).hasSize(1);
        assertThat(response.getKeywords()).contains("Nhà");
    }

    @Test
    @DisplayName("suggest - Thất bại khi từ khóa dưới 2 ký tự")
    void testSuggest_TooShort() {
        assertThatThrownBy(() -> bookService.suggest("a"))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_PARAMETER));
    }

    @Test
    @DisplayName("getBookDetail - Thành công trả về chi tiết cho khách vãng lai")
    void testGetBookDetail_Anonymous() {
        when(bookRepository.findBySlugAndStatus("nha-gia-kim", BookStatus.ACTIVE))
                .thenReturn(Optional.of(testBook));
        when(reviewRepository.countRatingsByStar(101L)).thenReturn(Collections.emptyList());

        BookDetailResponse detail = bookService.getBookDetail("nha-gia-kim", null);

        assertThat(detail).isNotNull();
        assertThat(detail.getTitle()).isEqualTo("Nhà Giả Kim");
        assertThat(detail.isInWishlist()).isFalse();
        assertThat(detail.getStockStatus()).isEqualTo(StockStatus.IN_STOCK);
        assertThat(detail.getMaxPurchasableQuantity()).isEqualTo(84);
    }

    @Test
    @DisplayName("getBookDetail - Thành công nhận diện sách trong Wishlist của người dùng")
    void testGetBookDetail_AuthenticatedInWishlist() {
        when(bookRepository.findBySlugAndStatus("nha-gia-kim", BookStatus.ACTIVE))
                .thenReturn(Optional.of(testBook));
        when(reviewRepository.countRatingsByStar(101L)).thenReturn(Collections.emptyList());

        Customer customer = Customer.builder().id(99L).build();
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(customer));
        when(wishlistRepository.existsByIdCustomerIdAndIdBookId(99L, 101L)).thenReturn(true);

        BookDetailResponse detail = bookService.getBookDetail("nha-gia-kim", 1L);

        assertThat(detail).isNotNull();
        assertThat(detail.isInWishlist()).isTrue();
    }

    @Test
    @DisplayName("getBookDetail - Ném lỗi BOOK_NOT_FOUND khi sách không tồn tại")
    void testGetBookDetail_NotFound() {
        when(bookRepository.findBySlugAndStatus("unknown", BookStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookDetail("unknown", null))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND));
    }

    @Test
    @DisplayName("getRelatedBooks - Thành công trả về sách cùng tác giả/thể loại")
    void testGetRelatedBooks_Success() {
        when(bookRepository.findBySlugAndStatus("nha-gia-kim", BookStatus.ACTIVE))
                .thenReturn(Optional.of(testBook));
        when(bookRepository.findRelatedBooksByAuthors(any(), eq(101L), any()))
                .thenReturn(List.of(testBook));

        List<BookCardResponse> related = bookService.getRelatedBooks("nha-gia-kim", 8);

        assertThat(related).hasSize(1);
    }

    @Test
    @DisplayName("getBookReviews - Thành công trả về danh sách đánh giá có mask tên khách hàng")
    void testGetBookReviews_Success() {
        when(bookRepository.findBySlugAndStatus("nha-gia-kim", BookStatus.ACTIVE))
                .thenReturn(Optional.of(testBook));

        Customer reviewer = Customer.builder().fullName("Nguyễn Tiến Chung").customerTier(CustomerTier.SILVER).build();
        Review review = Review.builder()
                .id(55L)
                .rating(5)
                .content("Sách rất hay!")
                .customer(reviewer)
                .status(ReviewStatus.VISIBLE)
                .createdAt(OffsetDateTime.now())
                .build();

        when(reviewRepository.findByBookIdAndStatus(eq(101L), eq(ReviewStatus.VISIBLE), any()))
                .thenReturn(new PageImpl<>(List.of(review)));

        PageResponse<BookReviewResponse> response = bookService.getBookReviews("nha-gia-kim", null, null, 0, 10, "createdAt,desc");

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getCustomer().getDisplayName()).isEqualTo("Nguyễn T. C.");
        assertThat(response.getItems().get(0).getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("getAdminBooks - Thành công trả về danh sách quản trị hiển thị tồn kho thật")
    void testGetAdminBooks_Success() {
        when(bookRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testBook)));

        PageResponse<AdminBookResponse> response = bookService.getAdminBooks("Nhà Giả Kim", "ACTIVE", null, 0, 20);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getStockQuantity()).isEqualTo(84);
        assertThat(response.getItems().get(0).getStatus()).isEqualTo(BookStatus.ACTIVE);
    }

    @Test
    @DisplayName("getAdminBookDetail - Thành công trả về chi tiết kỹ thuật cho Admin")
    void testGetAdminBookDetail_Success() {
        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));

        AdminBookDetailResponse detail = bookService.getAdminBookDetail(101L);

        assertThat(detail).isNotNull();
        assertThat(detail.getStockQuantity()).isEqualTo(84);
        assertThat(detail.getLowStockThreshold()).isEqualTo(10);
    }
}
