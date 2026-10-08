package com.chungpr0.bookhub.modules.cart;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CartItemAvailability;
import com.chungpr0.bookhub.common.enums.CartMergeResult;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.cart.dto.request.AddToCartRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeItemRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartMergeRequest;
import com.chungpr0.bookhub.modules.cart.dto.request.CartSearchFilter;
import com.chungpr0.bookhub.modules.cart.dto.request.UpdateCartItemQuantityRequest;
import com.chungpr0.bookhub.modules.cart.dto.response.AdminCartResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartMergeResponse;
import com.chungpr0.bookhub.modules.cart.dto.response.CartResponse;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import com.chungpr0.bookhub.modules.cart.entity.CartItem;
import com.chungpr0.bookhub.modules.cart.mapper.CartMapper;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.cart.service.impl.CartServiceImpl;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookRepository bookRepository;

    @Spy
    private CartMapper cartMapper = new CartMapper();

    @InjectMocks
    private CartServiceImpl cartService;

    private Account testAccount;
    private Customer testCustomer;
    private Cart testCart;
    private Book testBook;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder().id(1L).username("0988888888").build();

        testCustomer = Customer.builder()
                .id(10L)
                .account(testAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .build();

        testCart = Cart.builder()
                .id(100L)
                .customer(testCustomer)
                .updatedAt(OffsetDateTime.now())
                .items(new ArrayList<>())
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

        testCartItem = CartItem.builder()
                .id(300L)
                .cart(testCart)
                .book(testBook)
                .quantity(2)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getCart - Thành công trả về thông tin giỏ hàng và tóm tắt tài chính")
    void testGetCart_Success() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.getCart(1L);

        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo(100L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getLineTotal()).isEqualTo(160000L);
        assertThat(response.getItems().get(0).getAvailability()).isEqualTo(CartItemAvailability.AVAILABLE);
        assertThat(response.getSummary().getSubtotal()).isEqualTo(160000L);
        assertThat(response.getSummary().getSelectableSubtotal()).isEqualTo(160000L);
        assertThat(response.getSummary().getItemCount()).isEqualTo(1);
        assertThat(response.getSummary().getTotalQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("addToCart - Thành công thêm sách mới vào giỏ hàng")
    void testAddToCart_NewItem_Success() {
        AddToCartRequest request = AddToCartRequest.builder()
                .bookId(200L)
                .quantity(3)
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByCartIdAndBookId(100L, 200L)).thenReturn(Optional.empty());
        when(cartItemRepository.countByCartId(100L)).thenReturn(0);
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.addToCart(1L, request);

        assertThat(response).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("addToCart - Thành công cộng dồn số lượng sách đã có trong giỏ")
    void testAddToCart_AccumulateQuantity_Success() {
        AddToCartRequest request = AddToCartRequest.builder()
                .bookId(200L)
                .quantity(3)
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByCartIdAndBookId(100L, 200L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.addToCart(1L, request);

        assertThat(response).isNotNull();
        assertThat(testCartItem.getQuantity()).isEqualTo(5);
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    @DisplayName("addToCart - Sách không tồn tại ném BOOK_NOT_FOUND (404)")
    void testAddToCart_BookNotFound() {
        AddToCartRequest request = AddToCartRequest.builder().bookId(999L).quantity(1).build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("addToCart - Sách ngừng kinh doanh ném BOOK_NOT_AVAILABLE (422)")
    void testAddToCart_BookInactive() {
        testBook.setStatus(BookStatus.INACTIVE);
        AddToCartRequest request = AddToCartRequest.builder().bookId(200L).quantity(1).build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> cartService.addToCart(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BOOK_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("addToCart - Sách hết hàng ném OUT_OF_STOCK (422)")
    void testAddToCart_OutOfStock() {
        testBook.setStockQuantity(0);
        AddToCartRequest request = AddToCartRequest.builder().bookId(200L).quantity(1).build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> cartService.addToCart(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("addToCart - Vượt quá tồn kho khả dụng ném INSUFFICIENT_STOCK (422)")
    void testAddToCart_InsufficientStock() {
        testBook.setStockQuantity(5);
        testCartItem.setQuantity(4);
        AddToCartRequest request = AddToCartRequest.builder().bookId(200L).quantity(3).build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByCartIdAndBookId(100L, 200L)).thenReturn(Optional.of(testCartItem));

        assertThatThrownBy(() -> cartService.addToCart(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("addToCart - Vượt quá 50 đầu sách khác nhau ném CART_LIMIT_EXCEEDED (422)")
    void testAddToCart_CartLimitExceeded() {
        AddToCartRequest request = AddToCartRequest.builder().bookId(200L).quantity(1).build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(cartItemRepository.findByCartIdAndBookId(100L, 200L)).thenReturn(Optional.empty());
        when(cartItemRepository.countByCartId(100L)).thenReturn(50);

        assertThatThrownBy(() -> cartService.addToCart(1L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CART_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("updateItemQuantity - Cập nhật số lượng chốt cuối thành công")
    void testUpdateItemQuantity_Success() {
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder().quantity(10).build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndBookId(100L, 200L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(testCartItem));

        CartResponse response = cartService.updateItemQuantity(1L, 200L, request);

        assertThat(response).isNotNull();
        assertThat(testCartItem.getQuantity()).isEqualTo(10);
        verify(cartItemRepository).save(testCartItem);
    }

    @Test
    @DisplayName("updateItemQuantity - Sản phẩm không có trong giỏ ném CART_ITEM_NOT_FOUND (404)")
    void testUpdateItemQuantity_NotFound() {
        UpdateCartItemQuantityRequest request = UpdateCartItemQuantityRequest.builder().quantity(5).build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndBookId(100L, 999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItemQuantity(1L, 999L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("removeItem - Xóa thành công một cuốn sách khỏi giỏ hàng")
    void testRemoveItem_Success() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndBookId(100L, 200L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of());

        CartResponse response = cartService.removeItem(1L, 200L);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).isEmpty();
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    @DisplayName("clearCart - Làm trống giỏ hàng thành công")
    void testClearCart_Success() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));

        CartResponse response = cartService.clearCart(1L);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).isEmpty();
        verify(cartItemRepository).deleteAllByCartId(100L);
    }

    @Test
    @DisplayName("mergeCart - Cơ chế Smart Merge tự động điều chỉnh tồn kho và báo cáo")
    void testMergeCart_SmartMerge_Success() {
        Book limitedBook = Book.builder()
                .id(201L)
                .title("Cây Cam Ngọt Của Tôi")
                .status(BookStatus.ACTIVE)
                .stockQuantity(3)
                .authors(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        CartMergeRequest request = CartMergeRequest.builder()
                .items(List.of(
                        CartMergeItemRequest.builder().bookId(200L).quantity(2).build(),
                        CartMergeItemRequest.builder().bookId(201L).quantity(10).build()
                ))
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(10L)).thenReturn(Optional.of(testCart));
        when(bookRepository.findById(200L)).thenReturn(Optional.of(testBook));
        when(bookRepository.findById(201L)).thenReturn(Optional.of(limitedBook));
        when(cartItemRepository.findByCartIdAndBookId(100L, 200L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.findByCartIdAndBookId(100L, 201L)).thenReturn(Optional.empty());
        when(cartItemRepository.findByCartId(100L)).thenReturn(List.of(testCartItem));

        CartMergeResponse response = cartService.mergeCart(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getMergeReport()).hasSize(2);
        assertThat(response.getMergeReport().get(0).getResult()).isEqualTo(CartMergeResult.MERGED);
        assertThat(response.getMergeReport().get(1).getResult()).isEqualTo(CartMergeResult.ADJUSTED);
        assertThat(response.getMergeReport().get(1).getMergedQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("searchCarts - Admin tra cứu giỏ hàng thành công với phân trang")
    void testSearchCarts_Admin_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cart> cartPage = new PageImpl<>(List.of(testCart), pageable, 1);

        when(cartRepository.findAll(ArgumentMatchers.<Specification<Cart>>any(), eq(pageable))).thenReturn(cartPage);

        PageResponse<AdminCartResponse> response = cartService.searchCarts(new CartSearchFilter(), pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getPage().getTotalElements()).isEqualTo(1L);
    }
}
