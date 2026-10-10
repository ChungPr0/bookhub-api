package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.user.dto.request.AdjustPointsRequest;
import com.chungpr0.bookhub.modules.user.dto.request.CustomerFilterRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateCustomerStatusRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerDetailResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerOrderHistoryResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerStatusResponse;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerSummaryResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointAdjustmentResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointTransactionResponse;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.modules.user.service.impl.AdminCustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
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
class AdminCustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private AdminCustomerServiceImpl adminCustomerService;

    private Customer testCustomer;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(2001L)
                .username("0988888888")
                .passwordHash("hashed")
                .status(AccountStatus.ACTIVE)
                .tokenVersion(1)
                .build();

        testCustomer = Customer.builder()
                .id(1001L)
                .account(testAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .email("chung@example.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2000, 1, 1))
                .rewardPoints(1000)
                .totalSpent(2500000L)
                .customerTier(CustomerTier.SILVER)
                .build();
    }

    @Test
    @DisplayName("getCustomers: trả về danh sách khách hàng phân trang thành công")
    void getCustomers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(testCustomer), pageable, 1);

        when(customerRepository.findAll(ArgumentMatchers.<Specification<Customer>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(orderRepository.countByCustomerId(1001L)).thenReturn(5L);

        PageResponse<CustomerSummaryResponse> response = adminCustomerService.getCustomers(new CustomerFilterRequest(), pageable);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getFullName()).isEqualTo("Nguyễn Tiến Chung");
        assertThat(response.getItems().get(0).getOrderCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getCustomerDetail: trả về chi tiết khách hàng và thống kê thành công")
    void getCustomerDetail_Success() {
        when(customerRepository.findById(1001L)).thenReturn(Optional.of(testCustomer));
        Address address = Address.builder()
                .id(10L)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 10")
                .isDefault(true)
                .build();
        when(addressRepository.findByCustomerIdOrderByIsDefaultDescUpdatedAtDesc(1001L))
                .thenReturn(List.of(address));
        when(orderRepository.countByCustomerId(1001L)).thenReturn(4L);
        when(orderRepository.countByCustomerIdAndStatus(1001L, OrderStatus.COMPLETED)).thenReturn(3L);
        when(orderRepository.countByCustomerIdAndStatus(1001L, OrderStatus.CANCELLED)).thenReturn(1L);
        when(orderRepository.countByCustomerIdAndStatus(1001L, OrderStatus.RETURNED)).thenReturn(0L);

        CustomerDetailResponse response = adminCustomerService.getCustomerDetail(1001L);

        assertThat(response.getId()).isEqualTo(1001L);
        assertThat(response.getAddresses()).hasSize(1);
        assertThat(response.getOrderStats().getTotalOrders()).isEqualTo(4L);
        assertThat(response.getOrderStats().getCompletedOrders()).isEqualTo(3L);
        assertThat(response.getTierProgress().getCurrentTier()).isEqualTo(CustomerTier.SILVER);
    }

    @Test
    @DisplayName("getCustomerDetail: ném lỗi CUSTOMER_NOT_FOUND khi id không tồn tại")
    void getCustomerDetail_NotFound() {
        when(customerRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminCustomerService.getCustomerDetail(9999L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Test
    @DisplayName("updateCustomer: cập nhật thông tin thành công")
    void updateCustomer_Success() {
        when(customerRepository.findById(1001L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmailAndIdNot("new@example.com", 1001L)).thenReturn(false);
        when(orderRepository.countByCustomerId(1001L)).thenReturn(5L);

        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .fullName("Nguyễn Văn B")
                .email("new@example.com")
                .gender(Gender.OTHER)
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        CustomerSummaryResponse response = adminCustomerService.updateCustomer(1001L, request);

        assertThat(response.getFullName()).isEqualTo("Nguyễn Văn B");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        verify(customerRepository).save(testCustomer);
    }

    @Test
    @DisplayName("updateCustomer: ném lỗi EMAIL_ALREADY_IN_USE khi email đã tồn tại ở khách khác")
    void updateCustomer_EmailDuplicate() {
        when(customerRepository.findById(1001L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmailAndIdNot("duplicate@example.com", 1001L)).thenReturn(true);

        UpdateCustomerRequest request = UpdateCustomerRequest.builder()
                .fullName("Nguyễn Văn B")
                .email("duplicate@example.com")
                .gender(Gender.MALE)
                .build();

        assertThatThrownBy(() -> adminCustomerService.updateCustomer(1001L, request))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_IN_USE);
    }

    @Test
    @DisplayName("updateCustomerStatus: khóa tài khoản và tăng tokenVersion thành công")
    void updateCustomerStatus_LockAccount_Success() {
        when(customerRepository.findById(1001L)).thenReturn(Optional.of(testCustomer));

        UpdateCustomerStatusRequest request = UpdateCustomerStatusRequest.builder()
                .status(AccountStatus.LOCKED)
                .reason("Gian lận đơn hàng")
                .build();

        CustomerStatusResponse response = adminCustomerService.updateCustomerStatus(1001L, request);

        assertThat(response.getStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(testAccount.getTokenVersion()).isEqualTo(2);
        assertThat(testAccount.getLockedReason()).isEqualTo("Gian lận đơn hàng");
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("adjustPoints: cộng bù điểm thưởng thành công")
    void adjustPoints_Positive_Success() {
        when(customerRepository.findById(1001L)).thenReturn(Optional.of(testCustomer));
        when(pointTransactionRepository.save(any(PointTransaction.class))).thenAnswer(invocation -> {
            PointTransaction tx = invocation.getArgument(0);
            tx.setId(500L);
            tx.setCreatedAt(OffsetDateTime.now());
            return tx;
        });

        AdjustPointsRequest request = AdjustPointsRequest.builder()
                .points(500)
                .reason("Đền bù giao chậm")
                .build();

        PointAdjustmentResponse response = adminCustomerService.adjustPoints(1001L, request, 1L);

        assertThat(response.getPoints()).isEqualTo(500);
        assertThat(response.getBalanceAfter()).isEqualTo(1500);
        assertThat(testCustomer.getRewardPoints()).isEqualTo(1500);
        verify(customerRepository).save(testCustomer);
    }

    @Test
    @DisplayName("adjustPoints: ném lỗi INSUFFICIENT_POINTS khi trừ điểm lớn hơn số dư hiện có")
    void adjustPoints_InsufficientPoints() {
        when(customerRepository.findById(1001L)).thenReturn(Optional.of(testCustomer));

        AdjustPointsRequest request = AdjustPointsRequest.builder()
                .points(-1500)
                .reason("Trừ điểm phạt")
                .build();

        assertThatThrownBy(() -> adminCustomerService.adjustPoints(1001L, request, 1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_POINTS);
    }

    @Test
    @DisplayName("getCustomerOrders: trả về lịch sử đơn hàng phân trang")
    void getCustomerOrders_Success() {
        when(customerRepository.existsById(1001L)).thenReturn(true);
        Order order = Order.builder()
                .id(50L)
                .orderCode("ORD-123")
                .finalAmount(300000L)
                .status(OrderStatus.COMPLETED)
                .orderDetails(new ArrayList<>())
                .build();
        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findByCustomerId(1001L, PageRequest.of(0, 10))).thenReturn(orderPage);

        PageResponse<CustomerOrderHistoryResponse> response = adminCustomerService.getCustomerOrders(1001L, null, PageRequest.of(0, 10));

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getOrderCode()).isEqualTo("ORD-123");
    }

    @Test
    @DisplayName("getCustomerPointsHistory: trả về lịch sử giao dịch điểm phân trang")
    void getCustomerPointsHistory_Success() {
        when(customerRepository.existsById(1001L)).thenReturn(true);
        PointTransaction tx = PointTransaction.builder()
                .id(1L)
                .type(PointTransactionType.EARN)
                .points(100)
                .balanceAfter(1100)
                .build();
        Page<PointTransaction> txPage = new PageImpl<>(List.of(tx));
        when(pointTransactionRepository.findAll(ArgumentMatchers.<Specification<PointTransaction>>any(), any(Pageable.class)))
                .thenReturn(txPage);

        PageResponse<PointTransactionResponse> response = adminCustomerService.getCustomerPointsHistory(1001L, PageRequest.of(0, 10));

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getPoints()).isEqualTo(100);
    }
}
