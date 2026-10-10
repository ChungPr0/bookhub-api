package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.user.dto.request.CreateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.StaffFilterRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRoleRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffStatusRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CreateStaffResponse;
import com.chungpr0.bookhub.modules.user.dto.response.ResetStaffPasswordResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffStatusResponse;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import com.chungpr0.bookhub.modules.user.service.impl.AdminStaffServiceImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminStaffServiceImpl adminStaffService;

    private Staff testStaff;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .id(2012L)
                .username("0911223344")
                .passwordHash("hashed")
                .role(Role.STAFF)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .build();

        testStaff = Staff.builder()
                .id(12L)
                .account(testAccount)
                .fullName("Trần Thị Thu")
                .email("thuthu@bookhub.vn")
                .createdBy(1L)
                .build();
    }

    @Test
    @DisplayName("getStaffs: trả về danh sách nhân viên phân trang thành công")
    void getStaffs_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Staff> page = new PageImpl<>(List.of(testStaff), pageable, 1);

        when(staffRepository.findAll(ArgumentMatchers.<Specification<Staff>>any(), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<StaffResponse> response = adminStaffService.getStaffs(new StaffFilterRequest(), pageable);

        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getFullName()).isEqualTo("Trần Thị Thu");
    }

    @Test
    @DisplayName("getStaffDetail: trả về thông tin chi tiết nhân viên thành công")
    void getStaffDetail_Success() {
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));

        StaffResponse response = adminStaffService.getStaffDetail(12L);

        assertThat(response.getId()).isEqualTo(12L);
        assertThat(response.getFullName()).isEqualTo("Trần Thị Thu");
        assertThat(response.getRole()).isEqualTo(Role.STAFF);
    }

    @Test
    @DisplayName("getStaffDetail: ném lỗi STAFF_NOT_FOUND khi id không tồn tại")
    void getStaffDetail_NotFound() {
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminStaffService.getStaffDetail(999L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STAFF_NOT_FOUND);
    }

    @Test
    @DisplayName("createStaff: tạo nhân viên mới với mật khẩu tạm thành công")
    void createStaff_Success() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .phone("0911223344")
                .fullName("Lê Văn Khoa")
                .email("khoale@bookhub.vn")
                .role(Role.STAFF)
                .build();

        when(accountRepository.existsByUsername("0911223344")).thenReturn(false);
        when(staffRepository.existsByEmail("khoale@bookhub.vn")).thenReturn(false);
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashed-pwd");

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account acc = inv.getArgument(0);
            acc.setId(2015L);
            return acc;
        });

        when(staffRepository.save(any(Staff.class))).thenAnswer(inv -> {
            Staff s = inv.getArgument(0);
            s.setId(15L);
            return s;
        });

        CreateStaffResponse response = adminStaffService.createStaff(request, 1L);

        assertThat(response.getId()).isEqualTo(15L);
        assertThat(response.getFullName()).isEqualTo("Lê Văn Khoa");
        assertThat(response.getStatus()).isEqualTo(AccountStatus.UNVERIFIED);
        assertThat(response.getTemporaryPassword()).isNotNull().hasSize(12);
    }

    @Test
    @DisplayName("createStaff: ném lỗi PHONE_ALREADY_REGISTERED khi số điện thoại trùng")
    void createStaff_PhoneDuplicate() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .phone("0911223344")
                .fullName("Lê Văn Khoa")
                .email("khoale@bookhub.vn")
                .role(Role.STAFF)
                .build();

        when(accountRepository.existsByUsername("0911223344")).thenReturn(true);

        assertThatThrownBy(() -> adminStaffService.createStaff(request, 1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PHONE_ALREADY_REGISTERED);
    }

    @Test
    @DisplayName("createStaff: ném lỗi EMAIL_ALREADY_IN_USE khi email trùng")
    void createStaff_EmailDuplicate() {
        CreateStaffRequest request = CreateStaffRequest.builder()
                .phone("0911223344")
                .fullName("Lê Văn Khoa")
                .email("khoale@bookhub.vn")
                .role(Role.STAFF)
                .build();

        when(accountRepository.existsByUsername("0911223344")).thenReturn(false);
        when(staffRepository.existsByEmail("khoale@bookhub.vn")).thenReturn(true);

        assertThatThrownBy(() -> adminStaffService.createStaff(request, 1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_ALREADY_IN_USE);
    }

    @Test
    @DisplayName("updateStaffRole: đổi quyền nhân viên và tăng tokenVersion thành công")
    void updateStaffRole_Success() {
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));

        UpdateStaffRoleRequest request = UpdateStaffRoleRequest.builder()
                .role(Role.MANAGER)
                .build();

        adminStaffService.updateStaffRole(12L, request, 1L);

        assertThat(testAccount.getRole()).isEqualTo(Role.MANAGER);
        assertThat(testAccount.getTokenVersion()).isEqualTo(1);
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("updateStaffRole: ném lỗi SELF_MODIFICATION_FORBIDDEN khi tự đổi vai trò của mình")
    void updateStaffRole_SelfModificationForbidden() {
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));

        UpdateStaffRoleRequest request = UpdateStaffRoleRequest.builder()
                .role(Role.MANAGER)
                .build();

        // Calling with the same account ID (2012L)
        assertThatThrownBy(() -> adminStaffService.updateStaffRole(12L, request, 2012L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELF_MODIFICATION_FORBIDDEN);
    }

    @Test
    @DisplayName("updateStaffRole: ném lỗi LAST_ADMIN_PROTECTION khi hạ quyền Admin duy nhất")
    void updateStaffRole_LastAdminProtection() {
        testAccount.setRole(Role.ADMIN);
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));
        when(accountRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE)).thenReturn(1L);

        UpdateStaffRoleRequest request = UpdateStaffRoleRequest.builder()
                .role(Role.MANAGER)
                .build();

        assertThatThrownBy(() -> adminStaffService.updateStaffRole(12L, request, 1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LAST_ADMIN_PROTECTION);
    }

    @Test
    @DisplayName("updateStaffStatus: khóa tài khoản nhân viên thành công")
    void updateStaffStatus_Lock_Success() {
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));

        UpdateStaffStatusRequest request = UpdateStaffStatusRequest.builder()
                .status(AccountStatus.LOCKED)
                .reason("Nghỉ việc")
                .build();

        StaffStatusResponse response = adminStaffService.updateStaffStatus(12L, request, 1L);

        assertThat(response.getStatus()).isEqualTo(AccountStatus.LOCKED);
        assertThat(testAccount.getTokenVersion()).isEqualTo(1);
        assertThat(testAccount.getLockedReason()).isEqualTo("Nghỉ việc");
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("updateStaffStatus: ném lỗi SELF_MODIFICATION_FORBIDDEN khi tự khóa tài khoản của mình")
    void updateStaffStatus_SelfLockForbidden() {
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));

        UpdateStaffStatusRequest request = UpdateStaffStatusRequest.builder()
                .status(AccountStatus.LOCKED)
                .reason("Tự khóa")
                .build();

        assertThatThrownBy(() -> adminStaffService.updateStaffStatus(12L, request, 2012L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELF_MODIFICATION_FORBIDDEN);
    }

    @Test
    @DisplayName("updateStaffStatus: ném lỗi LAST_ADMIN_PROTECTION khi khóa Admin duy nhất")
    void updateStaffStatus_LastAdminProtection() {
        testAccount.setRole(Role.ADMIN);
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));
        when(accountRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE)).thenReturn(1L);

        UpdateStaffStatusRequest request = UpdateStaffStatusRequest.builder()
                .status(AccountStatus.LOCKED)
                .reason("Khóa Admin")
                .build();

        assertThatThrownBy(() -> adminStaffService.updateStaffStatus(12L, request, 1L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.LAST_ADMIN_PROTECTION);
    }

    @Test
    @DisplayName("resetStaffPassword: cấp lại mật khẩu tạm thành công")
    void resetStaffPassword_Success() {
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));
        when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("hashed-pwd");

        ResetStaffPasswordResponse response = adminStaffService.resetStaffPassword(12L, 1L);

        assertThat(response.getPhone()).isEqualTo("0911223344");
        assertThat(response.getTemporaryPassword()).isNotNull().hasSize(12);
        assertThat(testAccount.getStatus()).isEqualTo(AccountStatus.UNVERIFIED);
        assertThat(testAccount.getTokenVersion()).isEqualTo(1);
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("resetStaffPassword: ném lỗi SELF_MODIFICATION_FORBIDDEN khi tự reset cho mình")
    void resetStaffPassword_SelfResetForbidden() {
        when(staffRepository.findById(12L)).thenReturn(Optional.of(testStaff));

        assertThatThrownBy(() -> adminStaffService.resetStaffPassword(12L, 2012L))
                .isInstanceOf(AppException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.SELF_MODIFICATION_FORBIDDEN);
    }
}
