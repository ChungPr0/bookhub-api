package com.chungpr0.bookhub.modules.order;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.order.dto.request.CreateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ToggleVoucherStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AvailableVouchersResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherResponse;
import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
import com.chungpr0.bookhub.modules.order.service.impl.VoucherServiceImpl;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherUsageRepository voucherUsageRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private Customer testCustomer;
    private Voucher validVoucher;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(101L)
                .fullName("Nguyễn Tiến Chung")
                .build();

        validVoucher = Voucher.builder()
                .id(1L)
                .code("BOOKHUB10")
                .name("Giảm 10% đơn từ 200k")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(10L)
                .maxDiscountAmount(50_000L)
                .minOrderAmount(200_000L)
                .usageLimit(100)
                .usageLimitPerCustomer(2)
                .usedCount(0)
                .startDate(OffsetDateTime.now().minusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(10))
                .status(VoucherStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Get Available Vouchers - Phân loại chính xác voucher khả dụng và không khả dụng")
    void testGetAvailableVouchers_UsableAndUnusableSeparation() {
        Voucher expiredVoucher = Voucher.builder()
                .id(2L)
                .code("EXPIRED20")
                .name("Voucher hết hạn")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(20_000L)
                .minOrderAmount(100_000L)
                .usageLimit(50)
                .usageLimitPerCustomer(1)
                .usedCount(0)
                .startDate(OffsetDateTime.now().minusDays(10))
                .expirationDate(OffsetDateTime.now().minusDays(1))
                .status(VoucherStatus.ACTIVE)
                .build();

        Voucher highMinOrderVoucher = Voucher.builder()
                .id(3L)
                .code("VIP500")
                .name("Đơn từ 500k")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(50_000L)
                .minOrderAmount(500_000L)
                .usageLimit(50)
                .usageLimitPerCustomer(1)
                .usedCount(0)
                .startDate(OffsetDateTime.now().minusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(5))
                .status(VoucherStatus.ACTIVE)
                .build();

        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(voucherRepository.findAll()).thenReturn(List.of(validVoucher, expiredVoucher, highMinOrderVoucher));
        when(voucherUsageRepository.countByVoucherIdAndCustomerIdAndReleasedAtIsNull(1L, 101L)).thenReturn(0L);

        AvailableVouchersResponse response = voucherService.getAvailableVouchers(1L, 250_000L);

        assertThat(response).isNotNull();
        assertThat(response.getUsable()).hasSize(1);
        assertThat(response.getUsable().get(0).getCode()).isEqualTo("BOOKHUB10");
        assertThat(response.getUsable().get(0).getEstimatedDiscount()).isEqualTo(25_000L); // 10% of 250k

        assertThat(response.getUnusable()).hasSize(2);
        assertThat(response.getUnusable()).anyMatch(u -> "EXPIRED20".equals(u.getVoucher().getCode()) && "VOUCHER_EXPIRED".equals(u.getReasonCode()));
        assertThat(response.getUnusable()).anyMatch(u -> "VIP500".equals(u.getVoucher().getCode()) && "VOUCHER_MIN_ORDER_NOT_MET".equals(u.getReasonCode()));
    }

    @Test
    @DisplayName("Create Voucher - Thành công tạo voucher mới")
    void testCreateVoucher_Success() {
        CreateVoucherRequest request = CreateVoucherRequest.builder()
                .code("NEWYEAR2026")
                .name("Mừng năm mới 2026")
                .description("Giảm 20k cho mọi đơn")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(20_000L)
                .minOrderAmount(100_000L)
                .usageLimit(1000)
                .usageLimitPerCustomer(1)
                .startDate(OffsetDateTime.now().plusDays(1))
                .expirationDate(OffsetDateTime.now().plusDays(15))
                .build();

        when(voucherRepository.existsByCode("NEWYEAR2026")).thenReturn(false);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(inv -> {
            Voucher v = inv.getArgument(0);
            v.setId(99L);
            return v;
        });

        VoucherResponse response = voucherService.createVoucher(request);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("NEWYEAR2026");
        assertThat(response.getDiscountType()).isEqualTo(VoucherDiscountType.FIXED_AMOUNT);
        assertThat(response.getDiscountValue()).isEqualTo(20_000L);
    }

    @Test
    @DisplayName("Create Voucher - Thất bại do trùng mã voucher (409 CONCURRENT_MODIFICATION)")
    void testCreateVoucher_DuplicateCode_Throws409() {
        CreateVoucherRequest request = CreateVoucherRequest.builder()
                .code("BOOKHUB10")
                .name("Trùng mã")
                .discountType(VoucherDiscountType.FIXED_AMOUNT)
                .discountValue(10_000L)
                .startDate(OffsetDateTime.now())
                .expirationDate(OffsetDateTime.now().plusDays(5))
                .build();

        when(voucherRepository.existsByCode("BOOKHUB10")).thenReturn(true);

        assertThatThrownBy(() -> voucherService.createVoucher(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONCURRENT_MODIFICATION);
    }

    @Test
    @DisplayName("Update Voucher - Thành công sửa đổi thuộc tính tài chính khi usedCount = 0")
    void testUpdateVoucher_Success_WhenUsedCountIsZero() {
        validVoucher.setUsedCount(0);

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(validVoucher));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateVoucherRequest request = UpdateVoucherRequest.builder()
                .name("Giảm 15% đơn từ 200k")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(15L)
                .maxDiscountAmount(60_000L)
                .minOrderAmount(250_000L)
                .expirationDate(OffsetDateTime.now().plusDays(20))
                .build();

        VoucherResponse response = voucherService.updateVoucher(1L, request);

        assertThat(response.getDiscountValue()).isEqualTo(15L);
        assertThat(response.getMaxDiscountAmount()).isEqualTo(60_000L);
        assertThat(response.getMinOrderAmount()).isEqualTo(250_000L);
    }

    @Test
    @DisplayName("Update Voucher - Thất bại sửa đổi tài chính khi usedCount > 0 (409 VOUCHER_ALREADY_USED)")
    void testUpdateVoucher_AlreadyUsed_Throws409_OnFinancialFields() {
        validVoucher.setUsedCount(5); // Đã có 5 đơn dùng

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(validVoucher));

        UpdateVoucherRequest request = UpdateVoucherRequest.builder()
                .name("Cố tình đổi discount")
                .discountType(VoucherDiscountType.PERCENTAGE)
                .discountValue(20L) // Thay đổi từ 10L thành 20L
                .expirationDate(OffsetDateTime.now().plusDays(20))
                .build();

        assertThatThrownBy(() -> voucherService.updateVoucher(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VOUCHER_ALREADY_USED);
    }

    @Test
    @DisplayName("Delete Voucher - Thành công xóa voucher khi usedCount = 0")
    void testDeleteVoucher_Success_WhenUsedCountIsZero() {
        validVoucher.setUsedCount(0);

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(validVoucher));

        voucherService.deleteVoucher(1L);

        verify(voucherRepository).delete(validVoucher);
    }

    @Test
    @DisplayName("Delete Voucher - Thất bại khi voucher đã từng được dùng (409 VOUCHER_ALREADY_USED)")
    void testDeleteVoucher_AlreadyUsed_Throws409() {
        validVoucher.setUsedCount(1);

        when(voucherRepository.findById(1L)).thenReturn(Optional.of(validVoucher));

        assertThatThrownBy(() -> voucherService.deleteVoucher(1L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VOUCHER_ALREADY_USED);
    }

    @Test
    @DisplayName("Toggle Status - Thành công bật/tắt trạng thái voucher")
    void testToggleStatus_Success() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(validVoucher));
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(inv -> inv.getArgument(0));

        ToggleVoucherStatusRequest request = ToggleVoucherStatusRequest.builder()
                .status(VoucherStatus.INACTIVE)
                .build();

        VoucherResponse response = voucherService.toggleStatus(1L, request);

        assertThat(response.getStatus()).isEqualTo(VoucherStatus.INACTIVE);
    }
}
