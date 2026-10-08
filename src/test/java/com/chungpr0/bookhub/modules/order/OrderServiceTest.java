package com.chungpr0.bookhub.modules.order;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.cart.entity.Cart;
import com.chungpr0.bookhub.modules.cart.entity.CartItem;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.cart.repository.CartRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.order.dto.request.CheckoutPreviewRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CreateOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.CustomerCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.response.CheckoutPreviewResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CreateOrderResponse;
import com.chungpr0.bookhub.modules.order.dto.response.CustomerOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ReorderReportResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.enums.OrderCancelReason;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentMethodRepository;
import com.chungpr0.bookhub.modules.order.repository.ShippingConfigRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
import com.chungpr0.bookhub.modules.order.service.PaymentService;
import com.chungpr0.bookhub.modules.order.service.impl.OrderServiceImpl;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherUsageRepository voucherUsageRepository;

    @Mock
    private ShippingConfigRepository shippingConfigRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private PaymentService paymentService;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private Account testAccount;
    private Customer testCustomer;
    private Cart testCart;
    private Book testBook;
    private CartItem testCartItem;
    private PaymentMethod codPaymentMethod;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .username("0988888888")
                .build();

        testCustomer = Customer.builder()
                .id(101L)
                .account(testAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .rewardPoints(500)
                .totalSpent(1_900_000L)
                .customerTier(CustomerTier.BRONZE)
                .build();

        testCart = Cart.builder()
                .id(501L)
                .customer(testCustomer)
                .build();

        testBook = Book.builder()
                .id(1001L)
                .title("Nhà Giả Kim")
                .salePrice(100_000L)
                .stockQuantity(10)
                .status(BookStatus.ACTIVE)
                .build();

        testCartItem = CartItem.builder()
                .id(901L)
                .cart(testCart)
                .book(testBook)
                .quantity(2)
                .build();

        codPaymentMethod = PaymentMethod.builder()
                .id(1L)
                .code(PaymentMethodCode.COD)
                .name("Thanh toán khi nhận hàng")
                .isActive(true)
                .build();

        testAddress = Address.builder()
                .id(11L)
                .customer(testCustomer)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 10, Trần Thái Tông")
                .build();
    }

    @Test
    @DisplayName("Preview Checkout - Thành công tính toán giá tiền và phí vận chuyển")
    void testPreviewCheckout_Success() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(101L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(501L)).thenReturn(List.of(testCartItem));

        CheckoutPreviewRequest request = CheckoutPreviewRequest.builder()
                .bookIds(List.of(1001L))
                .build();

        CheckoutPreviewResponse response = orderService.previewCheckout(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getPricing().getSubtotal()).isEqualTo(200_000L);
        assertThat(response.getPricing().getShippingFee()).isEqualTo(30_000L);
        assertThat(response.getPricing().getFinalAmount()).isEqualTo(230_000L);
    }

    @Test
    @DisplayName("Preview Checkout - Thất bại khi bookIds rỗng (422 CART_EMPTY)")
    void testPreviewCheckout_CartEmpty_Throws422() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));

        CheckoutPreviewRequest request = CheckoutPreviewRequest.builder()
                .bookIds(List.of())
                .build();

        assertThatThrownBy(() -> orderService.previewCheckout(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_EMPTY);
    }

    @Test
    @DisplayName("Preview Checkout - Thất bại khi sách không có trong giỏ hàng (422 ITEMS_NOT_IN_CART)")
    void testPreviewCheckout_ItemsNotInCart_Throws422() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(101L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(501L)).thenReturn(List.of(testCartItem));

        CheckoutPreviewRequest request = CheckoutPreviewRequest.builder()
                .bookIds(List.of(9999L)) // Sách không có trong giỏ
                .build();

        assertThatThrownBy(() -> orderService.previewCheckout(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEMS_NOT_IN_CART);
    }

    @Test
    @DisplayName("Preview Checkout - Thất bại khi số lượng yêu cầu vượt quá tồn kho (422 INSUFFICIENT_STOCK)")
    void testPreviewCheckout_InsufficientStock_Throws422() {
        testBook.setStockQuantity(1); // Chỉ còn 1 cuốn trong kho mà giỏ có 2 cuốn
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(cartRepository.findByCustomerId(101L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(501L)).thenReturn(List.of(testCartItem));

        CheckoutPreviewRequest request = CheckoutPreviewRequest.builder()
                .bookIds(List.of(1001L))
                .build();

        assertThatThrownBy(() -> orderService.previewCheckout(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("Create Order - Thành công tạo đơn hàng COD và trừ tồn kho")
    void testCreateOrder_Success_COD() {
        when(orderRepository.findByIdempotencyKey("test-uuid")).thenReturn(Optional.empty());
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findById(11L)).thenReturn(Optional.of(testAddress));
        when(paymentMethodRepository.findByCode(PaymentMethodCode.COD)).thenReturn(Optional.of(codPaymentMethod));
        when(cartRepository.findByCustomerId(101L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(501L)).thenReturn(List.of(testCartItem));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(8801L);
            o.setCreatedAt(OffsetDateTime.now());
            return o;
        });

        CreateOrderRequest request = CreateOrderRequest.builder()
                .bookIds(List.of(1001L))
                .addressId(11L)
                .paymentMethod(PaymentMethodCode.COD)
                .expectedFinalAmount(230_000L) // 200k sách + 30k ship
                .build();

        CreateOrderResponse response = orderService.createOrder(1L, "test-uuid", request, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getOrder().getOrderCode()).startsWith("ORD-");
        assertThat(response.getOrder().getPricing().getFinalAmount()).isEqualTo(230_000L);
        assertThat(testBook.getStockQuantity()).isEqualTo(8); // 10 - 2 = 8
        verify(cartItemRepository).deleteByCartIdAndBookIdIn(eq(501L), ArgumentMatchers.<Collection<Long>>any());
    }

    @Test
    @DisplayName("Create Order - Thất bại do trượt giá lệch expectedFinalAmount (409 PRICE_CHANGED)")
    void testCreateOrder_PriceChanged_Throws409() {
        when(orderRepository.findByIdempotencyKey("test-uuid")).thenReturn(Optional.empty());
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findById(11L)).thenReturn(Optional.of(testAddress));
        when(paymentMethodRepository.findByCode(PaymentMethodCode.COD)).thenReturn(Optional.of(codPaymentMethod));
        when(cartRepository.findByCustomerId(101L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(501L)).thenReturn(List.of(testCartItem));

        CreateOrderRequest request = CreateOrderRequest.builder()
                .bookIds(List.of(1001L))
                .addressId(11L)
                .paymentMethod(PaymentMethodCode.COD)
                .expectedFinalAmount(150_000L) // Kỳ vọng 150k nhưng thực tế 230k
                .build();

        assertThatThrownBy(() -> orderService.createOrder(1L, "test-uuid", request, "127.0.0.1"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRICE_CHANGED);
    }

    @Test
    @DisplayName("Create Order - Trả về đơn hàng cũ khi trùng Idempotency-Key")
    void testCreateOrder_IdempotencyKeyReused_ReturnsExistingOrder() {
        Order existing = Order.builder()
                .id(8801L)
                .orderCode("ORD-20261008-EXIST")
                .customer(testCustomer)
                .paymentMethod(codPaymentMethod)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .shippingAddress("Hà Nội")
                .subtotalAmount(200_000L)
                .shippingFee(30_000L)
                .voucherDiscount(0L)
                .pointsUsed(0)
                .pointsDiscount(0L)
                .finalAmount(230_000L)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .createdAt(OffsetDateTime.now())
                .orderDetails(new ArrayList<>())
                .statusHistories(new ArrayList<>())
                .build();

        when(orderRepository.findByIdempotencyKey("existing-key")).thenReturn(Optional.of(existing));

        CreateOrderRequest request = CreateOrderRequest.builder()
                .bookIds(List.of(1001L))
                .addressId(11L)
                .paymentMethod(PaymentMethodCode.COD)
                .expectedFinalAmount(230_000L)
                .build();

        CreateOrderResponse response = orderService.createOrder(1L, "existing-key", request, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getOrder().getOrderCode()).isEqualTo("ORD-20261008-EXIST");
    }

    @Test
    @DisplayName("Cancel Order - Khách hàng tự hủy đơn PENDING thành công và hoàn trả tồn kho")
    void testCancelOrder_Success() {
        OrderDetail detail = OrderDetail.builder()
                .book(testBook)
                .quantity(2)
                .build();

        Order order = Order.builder()
                .id(8801L)
                .orderCode("ORD-20261008-TEST")
                .customer(testCustomer)
                .paymentMethod(codPaymentMethod)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .orderDetails(List.of(detail))
                .statusHistories(new ArrayList<>())
                .pointsUsed(100)
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-20261008-TEST", 101L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerCancelOrderRequest request = CustomerCancelOrderRequest.builder()
                .reason(OrderCancelReason.CHANGE_OF_MIND)
                .reasonNote("Đổi ý không mua nữa")
                .build();

        CustomerOrderDetailResponse response = orderService.cancelOrder(1L, "ORD-20261008-TEST", request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(testBook.getStockQuantity()).isEqualTo(12); // Hoàn lại 2 cuốn: 10 + 2 = 12
        assertThat(testCustomer.getRewardPoints()).isEqualTo(600); // Hoàn lại 100 điểm: 500 + 100 = 600
    }

    @Test
    @DisplayName("Cancel Order - Thất bại khi đơn không ở trạng thái PENDING (409 ORDER_CANNOT_BE_CANCELLED)")
    void testCancelOrder_NotPending_Throws409() {
        Order order = Order.builder()
                .id(8801L)
                .orderCode("ORD-20261008-TEST")
                .customer(testCustomer)
                .status(OrderStatus.CONFIRMED) // Đã xác nhận -> Không được tự hủy
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-20261008-TEST", 101L)).thenReturn(Optional.of(order));

        CustomerCancelOrderRequest request = CustomerCancelOrderRequest.builder()
                .reason(OrderCancelReason.CHANGE_OF_MIND)
                .build();

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "ORD-20261008-TEST", request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_CANNOT_BE_CANCELLED);
    }

    @Test
    @DisplayName("Confirm Received - Thành công xác nhận nhận hàng, tích điểm thưởng và thăng hạng SILVER")
    void testConfirmReceived_Success_EarnsPointsAndUpgradesTier() {
        Order order = Order.builder()
                .id(8801L)
                .orderCode("ORD-20261008-TEST")
                .customer(testCustomer)
                .paymentMethod(codPaymentMethod)
                .status(OrderStatus.SHIPPING)
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingFee(30_000L)
                .finalAmount(230_000L) // Tiền sách 200k => (230k - 30k) / 10k * 1.0 = 20 điểm
                .orderDetails(new ArrayList<>())
                .statusHistories(new ArrayList<>())
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-20261008-TEST", 101L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerOrderDetailResponse response = orderService.confirmReceived(1L, "ORD-20261008-TEST");

        assertThat(response.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(testCustomer.getRewardPoints()).isEqualTo(520); // 500 + 20
        assertThat(testCustomer.getTotalSpent()).isEqualTo(2_130_000L); // 1.9M + 230k = 2.13M >= 2M -> SILVER
        assertThat(testCustomer.getCustomerTier()).isEqualTo(CustomerTier.SILVER);
    }

    @Test
    @DisplayName("Reorder - Mua lại đơn cũ thành công thêm các sách vào giỏ")
    void testReorder_Success() {
        OrderDetail detail = OrderDetail.builder()
                .book(testBook)
                .bookTitle("Nhà Giả Kim")
                .quantity(2)
                .build();

        Order order = Order.builder()
                .id(8801L)
                .orderCode("ORD-20261008-OLD")
                .customer(testCustomer)
                .status(OrderStatus.COMPLETED)
                .orderDetails(List.of(detail))
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByOrderCodeAndCustomerId("ORD-20261008-OLD", 101L)).thenReturn(Optional.of(order));
        when(cartRepository.findByCustomerId(101L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndBookId(501L, 1001L)).thenReturn(Optional.empty());

        ReorderReportResponse response = orderService.reorder(1L, "ORD-20261008-OLD");

        assertThat(response.getTotalAdded()).isEqualTo(2);
        assertThat(response.getAddedItems()).hasSize(1);
        verify(cartItemRepository).save(any(CartItem.class));
    }
}
