package com.chungpr0.bookhub.modules.order;

import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.order.dto.request.AdminBankTransferConfirmRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminCancelOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminRefundOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminReturnOrderRequest;
import com.chungpr0.bookhub.modules.order.dto.request.AdminUpdateOrderStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AdminOrderDetailResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.OrderDetail;
import com.chungpr0.bookhub.modules.order.entity.Payment;
import com.chungpr0.bookhub.modules.order.entity.PaymentMethod;
import com.chungpr0.bookhub.modules.order.enums.OrderCancelReason;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.PaymentMethodCode;
import com.chungpr0.bookhub.modules.order.enums.PaymentStatus;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderStatusHistoryRepository;
import com.chungpr0.bookhub.modules.order.repository.PaymentRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
import com.chungpr0.bookhub.modules.order.service.impl.AdminOrderServiceImpl;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherUsageRepository voucherUsageRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private AdminOrderServiceImpl adminOrderService;

    private Customer testCustomer;
    private PaymentMethod vnpayMethod;
    private PaymentMethod bankTransferMethod;
    private Book testBook;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(101L)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .rewardPoints(500)
                .totalSpent(2_000_000L)
                .customerTier(CustomerTier.SILVER)
                .build();

        vnpayMethod = PaymentMethod.builder()
                .id(3L)
                .code(PaymentMethodCode.VNPAY)
                .name("Cổng VNPAY")
                .isActive(true)
                .build();

        bankTransferMethod = PaymentMethod.builder()
                .id(2L)
                .code(PaymentMethodCode.BANK_TRANSFER)
                .name("Chuyển khoản")
                .isActive(true)
                .build();

        testBook = Book.builder()
                .id(1001L)
                .title("Nhà Giả Kim")
                .salePrice(100_000L)
                .stockQuantity(10)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .id(501L)
                .book(testBook)
                .quantity(2)
                .unitPrice(100_000L)
                .lineTotal(200_000L)
                .costAmount(150_000L)
                .build();

        testOrder = Order.builder()
                .id(8801L)
                .orderCode("ORD-20261008-ADM")
                .customer(testCustomer)
                .paymentMethod(vnpayMethod)
                .subtotalAmount(200_000L)
                .shippingFee(30_000L)
                .finalAmount(230_000L)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .version(1)
                .orderDetails(List.of(detail))
                .statusHistories(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Admin Update Status - Thành công chuyển từ CONFIRMED sang SHIPPING khi đơn online đã PAID")
    void testUpdateOrderStatus_Success_ConfirmedToShipping() {
        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUpdateOrderStatusRequest request = AdminUpdateOrderStatusRequest.builder()
                .status(OrderStatus.SHIPPING)
                .note("Đã bàn giao shipper GHN")
                .version(1)
                .build();

        AdminOrderDetailResponse response = adminOrderService.updateOrderStatus(99L, 8801L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.SHIPPING);
        assertThat(testOrder.getShippedAt()).isNotNull();
    }

    @Test
    @DisplayName("Admin Update Status - Thất bại khi giao hàng đơn online chưa thanh toán (409 INVALID_ORDER_STATUS_TRANSITION)")
    void testUpdateOrderStatus_OnlineOrderUnpaid_Throws409() {
        testOrder.setPaymentStatus(PaymentStatus.UNPAID); // VNPAY nhưng chưa thanh toán
        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));

        AdminUpdateOrderStatusRequest request = AdminUpdateOrderStatusRequest.builder()
                .status(OrderStatus.SHIPPING)
                .version(1)
                .build();

        assertThatThrownBy(() -> adminOrderService.updateOrderStatus(99L, 8801L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
    }

    @Test
    @DisplayName("Admin Update Status - Thất bại do xung đột phiên bản Optimistic Lock (409 CONCURRENT_MODIFICATION)")
    void testUpdateOrderStatus_ConcurrentModification_Throws409() {
        testOrder.setVersion(2); // Phiên bản trong DB đã là 2
        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));

        AdminUpdateOrderStatusRequest request = AdminUpdateOrderStatusRequest.builder()
                .status(OrderStatus.SHIPPING)
                .version(1) // Client gửi version 1
                .build();

        assertThatThrownBy(() -> adminOrderService.updateOrderStatus(99L, 8801L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONCURRENT_MODIFICATION);
    }

    @Test
    @DisplayName("Admin Cancel Order - Cửa hàng hủy đơn thành công và hoàn trả kho")
    void testCancelOrder_Success_RestoresStock() {
        testOrder.setStatus(OrderStatus.CONFIRMED);
        testOrder.setPaymentStatus(PaymentStatus.PAID);
        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminCancelOrderRequest request = AdminCancelOrderRequest.builder()
                .reason(OrderCancelReason.DELIVERY_FAILED)
                .note("Khách không nhận hàng")
                .version(1)
                .build();

        AdminOrderDetailResponse response = adminOrderService.cancelOrder(99L, 8801L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
        assertThat(testBook.getStockQuantity()).isEqualTo(12); // Hoàn lại 2 cuốn: 10 + 2 = 12
    }

    @Test
    @DisplayName("Admin Confirm Bank Transfer - Khớp số tiền thành công chuyển sang PAID và CONFIRMED")
    void testConfirmBankTransferPayment_Success() {
        testOrder.setPaymentMethod(bankTransferMethod);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setPaymentStatus(PaymentStatus.UNPAID);
        testOrder.setFinalAmount(230_000L);

        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminBankTransferConfirmRequest request = AdminBankTransferConfirmRequest.builder()
                .amountReceived(230_000L)
                .bankTransactionRef("FT2628091245678")
                .note("Khớp sao kê VCB")
                .build();

        AdminOrderDetailResponse response = adminOrderService.confirmBankTransferPayment(99L, 8801L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Admin Confirm Bank Transfer - Thất bại do sai lệch số tiền nhận được (400 VALIDATION_FAILED)")
    void testConfirmBankTransferPayment_AmountMismatch_Throws400() {
        testOrder.setPaymentMethod(bankTransferMethod);
        testOrder.setPaymentStatus(PaymentStatus.UNPAID);
        testOrder.setFinalAmount(230_000L);

        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));

        AdminBankTransferConfirmRequest request = AdminBankTransferConfirmRequest.builder()
                .amountReceived(200_000L) // Thiếu 30k
                .bankTransactionRef("FT2628091245678")
                .build();

        assertThatThrownBy(() -> adminOrderService.confirmBankTransferPayment(99L, 8801L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VALIDATION_FAILED);
    }

    @Test
    @DisplayName("Admin Return Order - Tiếp nhận hoàn trả trong 7 ngày, thu hồi điểm và hạ chi tiêu")
    void testReturnOrder_Success_RevokesPoints() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        testOrder.setCompletedAt(OffsetDateTime.now().minusDays(2)); // Hoàn tất 2 ngày trước (trong hạn 7 ngày)
        testOrder.setPointsEarned(20);

        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminReturnOrderRequest request = AdminReturnOrderRequest.builder()
                .reason("Sách lỗi in ngược trang")
                .restock(true)
                .version(1)
                .build();

        AdminOrderDetailResponse response = adminOrderService.returnOrder(99L, 8801L, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.RETURNED);
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
        assertThat(testCustomer.getRewardPoints()).isEqualTo(480); // Thu hồi 20 điểm: 500 - 20 = 480
        assertThat(testBook.getStockQuantity()).isEqualTo(12); // restock = true: 10 + 2 = 12
    }

    @Test
    @DisplayName("Admin Return Order - Thất bại khi quá hạn 7 ngày hoàn trả (422 RETURN_WINDOW_EXPIRED)")
    void testReturnOrder_ExpiredReturnWindow_Throws422() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        testOrder.setCompletedAt(OffsetDateTime.now().minusDays(10)); // Đã hoàn tất 10 ngày trước

        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));

        AdminReturnOrderRequest request = AdminReturnOrderRequest.builder()
                .reason("Khách muốn trả sách")
                .restock(false)
                .version(1)
                .build();

        assertThatThrownBy(() -> adminOrderService.returnOrder(99L, 8801L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RETURN_WINDOW_EXPIRED);
    }

    @Test
    @DisplayName("Admin Refund Order - Hoàn tiền thành công cho đơn REFUND_PENDING")
    void testRefundOrder_Success() {
        testOrder.setPaymentStatus(PaymentStatus.REFUND_PENDING);

        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminRefundOrderRequest request = AdminRefundOrderRequest.builder()
                .refundAmount(230_000L)
                .refundMethod("BANK_TRANSFER")
                .refundTransactionRef("REFUND-12345")
                .note("Đã chuyển hoàn tiền về VCB của khách")
                .build();

        AdminOrderDetailResponse response = adminOrderService.refundOrder(99L, 8801L, "refund-key", request);

        assertThat(response).isNotNull();
        assertThat(testOrder.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("Admin Refund Order - Thất bại khi đơn không ở trạng thái REFUND_PENDING (422 REFUND_NOT_APPLICABLE)")
    void testRefundOrder_NotApplicable_Throws422() {
        testOrder.setPaymentStatus(PaymentStatus.UNPAID); // Không phải REFUND_PENDING

        when(orderRepository.findById(8801L)).thenReturn(Optional.of(testOrder));

        AdminRefundOrderRequest request = AdminRefundOrderRequest.builder()
                .refundAmount(230_000L)
                .refundMethod("BANK_TRANSFER")
                .refundTransactionRef("REFUND-12345")
                .build();

        assertThatThrownBy(() -> adminOrderService.refundOrder(99L, 8801L, "refund-key", request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFUND_NOT_APPLICABLE);
    }
}

