package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.exception.ValidationException;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateProfileRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerProfileResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointsOverviewResponse;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.entity.PointTransaction;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import com.chungpr0.bookhub.modules.user.service.impl.CustomerProfileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private CustomerProfileServiceImpl customerProfileService;

    private Customer testCustomer;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(1L)
                .username("0988888888")
                .build();

        testCustomer = Customer.builder()
                .id(10L)
                .account(testAccount)
                .fullName("Nguyễn Tiến Chung")
                .phone("0988888888")
                .email("chung@gmail.com")
                .gender(Gender.MALE)
                .birthday(LocalDate.of(2002, 4, 3))
                .avatarUrl("https://cdn.bookhub.vn/avatars/user-10.webp")
                .rewardPoints(1420)
                .totalSpent(1450000L)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("PRF-01: Lấy hồ sơ cá nhân thành công & tính đúng tierProgress BRONZE")
    void testGetProfileSuccess() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));

        CustomerProfileResponse response = customerProfileService.getProfile(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getFullName()).isEqualTo("Nguyễn Tiến Chung");
        assertThat(response.getTier()).isEqualTo(CustomerTier.BRONZE);
        assertThat(response.getTierProgress().getNextTier()).isEqualTo(CustomerTier.SILVER);
        assertThat(response.getTierProgress().getAmountToNextTier()).isEqualTo(550000L);
        assertThat(response.getTierProgress().getProgressPercent()).isEqualTo(72);
    }

    @Test
    @DisplayName("PRF-01: Lấy hồ sơ thất bại khi không tìm thấy Customer")
    void testGetProfileNotFound() {
        when(customerRepository.findByAccountId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerProfileService.getProfile(99L))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CUSTOMER_NOT_FOUND);
    }

    @Test
    @DisplayName("PRF-02: Cập nhật hồ sơ cá nhân thành công")
    void testUpdateProfileSuccess() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmailAndIdNot("newemail@gmail.com", 10L)).thenReturn(false);
        when(staffRepository.existsByEmail("newemail@gmail.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyễn Văn A")
                .email("newemail@gmail.com")
                .gender(Gender.FEMALE)
                .birthday(LocalDate.of(2000, 1, 1))
                .avatarUrl("https://cdn.bookhub.vn/avatars/new-avatar.webp")
                .build();

        CustomerProfileResponse response = customerProfileService.updateProfile(1L, request);

        assertThat(response.getFullName()).isEqualTo("Nguyễn Văn A");
        assertThat(response.getEmail()).isEqualTo("newemail@gmail.com");
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("PRF-02: Cập nhật hồ sơ thất bại khi URL avatar không đúng CDN BookHub")
    void testUpdateProfileInvalidAvatarUrl() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyễn Văn A")
                .gender(Gender.MALE)
                .avatarUrl("https://external-site.com/image.png")
                .build();

        assertThatThrownBy(() -> customerProfileService.updateProfile(1L, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Dữ liệu không hợp lệ");
    }

    @Test
    @DisplayName("PRF-02: Cập nhật hồ sơ thất bại khi email đã được người khác sử dụng")
    void testUpdateProfileEmailDuplicate() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByEmailAndIdNot("taken@gmail.com", 10L)).thenReturn(true);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .fullName("Nguyễn Văn A")
                .email("taken@gmail.com")
                .gender(Gender.MALE)
                .build();

        assertThatThrownBy(() -> customerProfileService.updateProfile(1L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_IN_USE);
    }

    @Test
    @DisplayName("PRF-03: Xem điểm thưởng và lịch sử giao dịch điểm thành công")
    void testGetPointsSuccess() {
        when(customerRepository.findByAccountId(1L)).thenReturn(Optional.of(testCustomer));

        PointTransaction tx = PointTransaction.builder()
                .id(101L)
                .customer(testCustomer)
                .type(PointTransactionType.EARN)
                .points(50)
                .balanceAfter(1420)
                .note("Tích điểm mua sách")
                .createdAt(OffsetDateTime.now())
                .build();

        Page<PointTransaction> page = new PageImpl<>(List.of(tx), PageRequest.of(0, 15), 1);
        when(pointTransactionRepository.findAll(org.mockito.ArgumentMatchers.<Specification<PointTransaction>>any(), any(Pageable.class))).thenReturn(page);

        PointsOverviewResponse response = customerProfileService.getPoints(1L, PointTransactionType.EARN, PageRequest.of(0, 15));

        assertThat(response).isNotNull();
        assertThat(response.getSummary().getRewardPoints()).isEqualTo(1420);
        assertThat(response.getSummary().getEquivalentValueVnd()).isEqualTo(142000L);
        assertThat(response.getHistory().getItems()).hasSize(1);
        assertThat(response.getHistory().getItems().get(0).getType()).isEqualTo(PointTransactionType.EARN);
    }
}

