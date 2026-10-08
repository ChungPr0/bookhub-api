package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.user.dto.request.AddressRequest;
import com.chungpr0.bookhub.modules.user.dto.response.AddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.DeleteAddressResponse;
import com.chungpr0.bookhub.modules.user.dto.response.SetDefaultAddressResponse;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.service.impl.CustomerAddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
class CustomerAddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerAddressServiceImpl customerAddressService;

    private Customer testCustomer;
    private Address testAddress1;
    private Address testAddress2;

    @BeforeEach
    void setUp() {
        Account testAccount = Account.builder().id(1L).username("0988888888").build();

        testCustomer = Customer.builder()
                .id(10L)
                .account(testAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .build();

        testAddress1 = Address.builder()
                .id(5L)
                .customer(testCustomer)
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng")
                .detailAddress("Số 10, Ngõ 2, Trần Thái Tông")
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        testAddress2 = Address.builder()
                .id(12L)
                .customer(testCustomer)
                .receiverName("Tiến Chung Văn Phòng")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Quận Đống Đa")
                .ward("Phường Láng Hạ")
                .detailAddress("14 Láng Hạ")
                .isDefault(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("ADR-01: Lấy danh sách địa chỉ thành công")
    void testGetAddressesSuccess() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findByCustomerIdOrderByIsDefaultDescUpdatedAtDesc(10L))
                .thenReturn(List.of(testAddress1, testAddress2));

        List<AddressResponse> result = customerAddressService.getAddresses(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).isDefault()).isTrue();
        assertThat(result.get(1).isDefault()).isFalse();
    }

    @Test
    @DisplayName("ADR-02: Lấy chi tiết một địa chỉ thành công")
    void testGetAddressByIdSuccess() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findByIdAndCustomerId(5L, 10L)).thenReturn(Optional.of(testAddress1));

        AddressResponse result = customerAddressService.getAddressById(1L, 5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getReceiverName()).isEqualTo("Nguyễn Tiến Chung");
    }

    @Test
    @DisplayName("ADR-02: Lấy chi tiết thất bại khi ID không tồn tại hoặc IDOR")
    void testGetAddressByIdNotFound() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findByIdAndCustomerId(999L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerAddressService.getAddressById(1L, 999L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_NOT_FOUND);
    }

    @Test
    @DisplayName("ADR-03: Thêm địa chỉ đầu tiên tự động thành mặc định")
    void testCreateAddressFirstIsDefault() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.countByCustomerId(10L)).thenReturn(0L);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address a = invocation.getArgument(0);
            a.setId(15L);
            return a;
        });

        AddressRequest request = AddressRequest.builder()
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng")
                .detailAddress("Số 10 Trần Thái Tông")
                .isDefault(false)
                .build();

        AddressResponse result = customerAddressService.createAddress(1L, request);

        assertThat(result.getId()).isEqualTo(15L);
        assertThat(result.isDefault()).isTrue();
    }

    @Test
    @DisplayName("ADR-03: Thêm địa chỉ thất bại khi vượt quá giới hạn 10 địa chỉ")
    void testCreateAddressLimitExceeded() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.countByCustomerId(10L)).thenReturn(10L);

        AddressRequest request = AddressRequest.builder()
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng")
                .detailAddress("Số 10 Trần Thái Tông")
                .build();

        assertThatThrownBy(() -> customerAddressService.createAddress(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ADDRESS_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("ADR-04: Cập nhật địa chỉ thành công")
    void testUpdateAddressSuccess() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findByIdAndCustomerId(5L, 10L)).thenReturn(Optional.of(testAddress1));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddressRequest request = AddressRequest.builder()
                .receiverName("Nguyễn Văn B")
                .receiverPhone("0977777777")
                .province("Hà Nội")
                .district("Quận Ba Đình")
                .ward("Phường Kim Mã")
                .detailAddress("Số 99 Kim Mã")
                .isDefault(true)
                .build();

        AddressResponse result = customerAddressService.updateAddress(1L, 5L, request);

        assertThat(result.getReceiverName()).isEqualTo("Nguyễn Văn B");
        assertThat(result.getReceiverPhone()).isEqualTo("0977777777");
        assertThat(result.getProvince()).isEqualTo("Hà Nội");
    }

    @Test
    @DisplayName("ADR-05: Xóa địa chỉ mặc định -> tự động gán địa chỉ còn lại làm mặc định mới")
    void testDeleteAddressDefaultReassigns() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findByIdAndCustomerId(5L, 10L)).thenReturn(Optional.of(testAddress1));
        when(addressRepository.findFirstByCustomerIdOrderByUpdatedAtDesc(10L)).thenReturn(Optional.of(testAddress2));

        DeleteAddressResponse response = customerAddressService.deleteAddress(1L, 5L);

        assertThat(response.getDeletedAddressId()).isEqualTo(5L);
        assertThat(response.getNewDefaultAddressId()).isEqualTo(12L);
        assertThat(testAddress2.isDefault()).isTrue();
        verify(addressRepository).delete(testAddress1);
        verify(addressRepository).save(testAddress2);
    }

    @Test
    @DisplayName("ADR-06: Đặt làm địa chỉ mặc định nhanh chóng")
    void testSetDefaultAddressSuccess() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(addressRepository.findByIdAndCustomerId(12L, 10L)).thenReturn(Optional.of(testAddress2));

        SetDefaultAddressResponse response = customerAddressService.setDefaultAddress(1L, 12L);

        assertThat(response.getId()).isEqualTo(12L);
        assertThat(response.isDefault()).isTrue();
        verify(addressRepository).resetDefaultAddressForCustomer(10L);
        verify(addressRepository).save(testAddress2);
    }
}

