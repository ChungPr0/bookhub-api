package com.chungpr0.bookhub.modules.cart;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.cart.dto.request.WishlistFilter;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistActionResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.WishlistItemResponse;
import com.chungpr0.bookhub.modules.cart.mapper.CartMapper;
import com.chungpr0.bookhub.modules.cart.service.impl.WishlistServiceImpl;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.entity.Wishlist;
import com.chungpr0.bookhub.modules.catalog.entity.WishlistId;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookRepository bookRepository;

    @Spy
    private CartMapper cartMapper = new CartMapper();

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private Customer testCustomer;
    private Book testBook;
    private Wishlist testWishlist;

    @BeforeEach
    void setUp() {
        Account testAccount = Account.builder().id(1L).username("0988888888").build();

        testCustomer = Customer.builder()
                .id(10L)
                .account(testAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .build();

        testBook = Book.builder()
                .id(200L)
                .title("Nhà Giả Kim")
                .slug("nha-gia-kim")
                .originalPrice(100000L)
                .salePrice(80000L)
                .status(BookStatus.ACTIVE)
                .stockQuantity(50)
                .authors(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        testWishlist = Wishlist.builder()
                .id(new WishlistId(10L, 200L))
                .customer(testCustomer)
                .book(testBook)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getWishlist - Lấy danh sách yêu thích thành công có phân trang")
    void testGetWishlist_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Wishlist> page = new PageImpl<>(List.of(testWishlist), pageable, 1);

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(wishlistRepository.findAll(ArgumentMatchers.<Specification<Wishlist>>any(), eq(pageable))).thenReturn(page);

        PageResponse<WishlistItemResponse> response = wishlistService.getWishlist(1L, new WishlistFilter(), pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getTitle()).isEqualTo("Nhà Giả Kim");
        assertThat(response.getPage().getTotalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("addToWishlist - Thêm mới sách vào danh sách yêu thích thành công")
    void testAddToWishlist_Success() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(wishlistRepository.existsByIdCustomerIdAndIdBookId(10L, 200L)).thenReturn(false);
        when(wishlistRepository.countByIdCustomerId(10L)).thenReturn(5L);

        WishlistActionResponse response = wishlistService.addToWishlist(1L, 200L);

        assertThat(response).isNotNull();
        assertThat(response.getBookId()).isEqualTo(200L);
        assertThat(response.isInWishlist()).isTrue();
        verify(wishlistRepository).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("addToWishlist - Sách đã có trong danh sách yêu thích trả về thành công Idempotent")
    void testAddToWishlist_Idempotent_AlreadyExists() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(wishlistRepository.existsByIdCustomerIdAndIdBookId(10L, 200L)).thenReturn(true);

        WishlistActionResponse response = wishlistService.addToWishlist(1L, 200L);

        assertThat(response).isNotNull();
        assertThat(response.isInWishlist()).isTrue();
        verify(wishlistRepository, never()).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("addToWishlist - Sách không tồn tại ném BOOK_NOT_FOUND (404)")
    void testAddToWishlist_BookNotFound() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 999L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("addToWishlist - Danh sách đã đạt 200 cuốn ném WISHLIST_LIMIT_EXCEEDED (422)")
    void testAddToWishlist_LimitExceeded() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(wishlistRepository.existsByIdCustomerIdAndIdBookId(10L, 200L)).thenReturn(false);
        when(wishlistRepository.countByIdCustomerId(10L)).thenReturn(200L);

        assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 200L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WISHLIST_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("removeFromWishlist - Xóa sách khỏi danh sách yêu thích thành công Idempotent")
    void testRemoveFromWishlist_Success() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));

        WishlistActionResponse response = wishlistService.removeFromWishlist(1L, 200L);

        assertThat(response).isNotNull();
        assertThat(response.getBookId()).isEqualTo(200L);
        assertThat(response.isInWishlist()).isFalse();
        verify(wishlistRepository).deleteByIdCustomerIdAndIdBookId(10L, 200L);
    }
}
