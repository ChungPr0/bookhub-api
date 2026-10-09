package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateBookRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateBookRequest;
import com.chungpr0.bookhub.modules.catalog.dto.response.AdminBookDetailResponse;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.entity.Publisher;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.PublisherRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.catalog.service.impl.BookServiceImpl;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookMutationServiceTest {

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

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book existingBook;
    private Category category;
    private Publisher publisher;
    private Author author;

    @BeforeEach
    void setUp() {
        category = Category.builder().id(10L).name("Công Nghệ").slug("cong-nghe").build();
        publisher = Publisher.builder().id(20L).name("NXB Trẻ").slug("nxb-tre").build();
        author = Author.builder().id(30L).name("Martin Fowler").slug("martin-fowler").build();

        existingBook = Book.builder()
                .id(1L)
                .title("Clean Architecture")
                .slug("clean-architecture")
                .isbn("9780134494166")
                .originalPrice(350000L)
                .salePrice(280000L)
                .stockQuantity(100)
                .lowStockThreshold(10)
                .status(BookStatus.ACTIVE)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .version(1)
                .category(category)
                .publisher(publisher)
                .authors(List.of(author))
                .images(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("createBook - Thành công tạo sách mới đầy đủ quan hệ và ảnh")
    void testCreateBook_Success() {
        CreateBookRequest request = CreateBookRequest.builder()
                .title("Refactoring")
                .isbn("9780201485677")
                .categoryId(10L)
                .publisherId(20L)
                .authorIds(List.of(30L))
                .originalPrice(400000L)
                .salePrice(320000L)
                .lowStockThreshold(5)
                .coverType(CoverType.HARDCOVER)
                .language("VI")
                .images(List.of("https://bookhub.com/img1.jpg"))
                .build();

        when(bookRepository.existsByIsbn("9780201485677")).thenReturn(false);
        when(bookRepository.existsBySlug("refactoring")).thenReturn(false);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(20L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findById(30L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(2L);
            return b;
        });
        when(bookRepository.findById(2L)).thenAnswer(inv -> {
            Book b = Book.builder()
                    .id(2L)
                    .title("Refactoring")
                    .slug("refactoring")
                    .category(category)
                    .publisher(publisher)
                    .authors(List.of(author))
                    .originalPrice(400000L)
                    .salePrice(320000L)
                    .images(new ArrayList<>())
                    .build();
            return Optional.of(b);
        });

        AdminBookDetailResponse response = bookService.createBook(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getTitle()).isEqualTo("Refactoring");
        assertThat(response.getSlug()).isEqualTo("refactoring");
    }

    @Test
    @DisplayName("createBook - Ném lỗi ISBN_ALREADY_EXISTS khi ISBN đã tồn tại")
    void testCreateBook_DuplicateIsbn() {
        CreateBookRequest request = CreateBookRequest.builder()
                .title("Refactoring")
                .isbn("9780134494166")
                .categoryId(10L)
                .publisherId(20L)
                .authorIds(List.of(30L))
                .originalPrice(400000L)
                .salePrice(320000L)
                .build();

        when(bookRepository.existsByIsbn("9780134494166")).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ISBN_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("createBook - Ném lỗi VALIDATION_FAILED khi giá bán lớn hơn giá gốc")
    void testCreateBook_SalePriceGreaterThanOriginal() {
        CreateBookRequest request = CreateBookRequest.builder()
                .title("Refactoring")
                .originalPrice(300000L)
                .salePrice(400000L)
                .build();

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("updateBook - Thành công cập nhật sách với version khớp (Optimistic Locking)")
    void testUpdateBook_Success() {
        UpdateBookRequest request = UpdateBookRequest.builder()
                .title("Clean Architecture 2nd Edition")
                .isbn("9780134494166")
                .categoryId(10L)
                .publisherId(20L)
                .authorIds(List.of(30L))
                .originalPrice(380000L)
                .salePrice(300000L)
                .lowStockThreshold(10)
                .coverType(CoverType.PAPERBACK)
                .language("VI")
                .version(1)
                .images(List.of("https://bookhub.com/img1.jpg"))
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByIsbnAndIdNot("9780134494166", 1L)).thenReturn(false);
        when(bookRepository.existsBySlugAndIdNot("clean-architecture-2nd-edition", 1L)).thenReturn(false);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(publisherRepository.findById(20L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findById(30L)).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        AdminBookDetailResponse response = bookService.updateBook(1L, request);

        assertThat(response).isNotNull();
        assertThat(existingBook.getTitle()).isEqualTo("Clean Architecture 2nd Edition");
        assertThat(existingBook.getSalePrice()).isEqualTo(300000L);
    }

    @Test
    @DisplayName("updateBook - Ném lỗi CONCURRENT_MODIFICATION khi version không khớp")
    void testUpdateBook_VersionMismatch() {
        UpdateBookRequest request = UpdateBookRequest.builder()
                .title("Clean Architecture 2nd Edition")
                .version(0) // Stale version
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));

        assertThatThrownBy(() -> bookService.updateBook(1L, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.CONCURRENT_MODIFICATION));
    }

    @Test
    @DisplayName("updateBookStatus - Thành công đổi trạng thái sách")
    void testUpdateBookStatus_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        bookService.updateBookStatus(1L, BookStatus.INACTIVE);

        assertThat(existingBook.getStatus()).isEqualTo(BookStatus.INACTIVE);
        verify(bookRepository).save(existingBook);
    }

    @Test
    @DisplayName("deleteBook - Ném lỗi BOOK_HAS_TRANSACTIONS khi sách đã phát sinh đơn hàng")
    void testDeleteBook_HasTransactions() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(orderDetailRepository.countByBookId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> bookService.deleteBook(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.BOOK_HAS_TRANSACTIONS));
    }

    @Test
    @DisplayName("deleteBook - Thành công dọn dẹp Cart, Wishlist và xóa sách")
    void testDeleteBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(orderDetailRepository.countByBookId(1L)).thenReturn(0L);

        bookService.deleteBook(1L);

        verify(cartItemRepository).deleteByBookId(1L);
        verify(wishlistRepository).deleteByBookId(1L);
        verify(bookRepository).delete(existingBook);
    }
}
