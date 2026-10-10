package com.chungpr0.bookhub.modules.user.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.PhoneUtils;
import com.chungpr0.bookhub.common.util.SecurePasswordGenerator;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.user.dto.request.CreateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.StaffFilterRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRoleRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffStatusRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CreateStaffResponse;
import com.chungpr0.bookhub.modules.user.dto.response.ResetStaffPasswordResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffStatusResponse;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import com.chungpr0.bookhub.modules.user.repository.specification.StaffSpecification;
import com.chungpr0.bookhub.modules.user.service.AdminStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStaffServiceImpl implements AdminStaffService {

    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StaffResponse> getStaffs(StaffFilterRequest filter, Pageable pageable) {
        Page<Staff> staffPage = staffRepository.findAll(StaffSpecification.filter(filter), pageable);

        List<StaffResponse> items = staffPage.getContent().stream()
                .map(StaffResponse::fromEntity)
                .toList();

        return PageResponse.of(items, staffPage);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffDetail(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));
        return StaffResponse.fromEntity(staff);
    }

    @Override
    @Transactional
    public CreateStaffResponse createStaff(CreateStaffRequest request, Long adminAccountId) {
        if (request.getRole() == Role.CUSTOMER) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Vai trò không hợp lệ cho nhân sự");
        }

        String normalizedPhone = PhoneUtils.normalize(request.getPhone());
        if (accountRepository.existsByUsername(normalizedPhone)) {
            throw new AppException(ErrorCode.PHONE_ALREADY_REGISTERED);
        }

        String email = request.getEmail().trim();
        if (staffRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        String temporaryPassword = SecurePasswordGenerator.generateTemporaryPassword();
        String hashedPassword = passwordEncoder.encode(temporaryPassword);

        Account account = Account.builder()
                .username(normalizedPhone)
                .passwordHash(hashedPassword)
                .role(request.getRole())
                .status(AccountStatus.UNVERIFIED)
                .tokenVersion(0)
                .failedLoginCount(0)
                .build();

        Account savedAccount = accountRepository.save(account);

        Staff staff = Staff.builder()
                .account(savedAccount)
                .fullName(request.getFullName().trim())
                .email(email)
                .createdBy(adminAccountId)
                .build();

        Staff savedStaff = staffRepository.save(staff);

        return CreateStaffResponse.builder()
                .id(savedStaff.getId())
                .phone(savedAccount.getUsername())
                .fullName(savedStaff.getFullName())
                .role(savedAccount.getRole())
                .status(savedAccount.getStatus())
                .temporaryPassword(temporaryPassword)
                .build();
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(Long id, UpdateStaffRequest request) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        String email = request.getEmail().trim();
        if (!email.equalsIgnoreCase(staff.getEmail()) && staffRepository.existsByEmailAndIdNot(email, id)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_IN_USE);
        }

        staff.setFullName(request.getFullName().trim());
        staff.setEmail(email);

        Staff savedStaff = staffRepository.save(staff);
        return StaffResponse.fromEntity(savedStaff);
    }

    @Override
    @Transactional
    public StaffResponse updateStaffRole(Long id, UpdateStaffRoleRequest request, Long currentAdminAccountId) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        Account account = staff.getAccount();
        if (account == null) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }

        // BR-U01: Self modification forbidden
        if (currentAdminAccountId != null && account.getId().equals(currentAdminAccountId)) {
            throw new AppException(ErrorCode.SELF_MODIFICATION_FORBIDDEN);
        }

        if (request.getRole() == Role.CUSTOMER) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Vai trò không hợp lệ cho nhân sự");
        }

        // BR-U02: Last admin protection
        if (account.getRole() == Role.ADMIN && request.getRole() != Role.ADMIN) {
            long activeAdminCount = accountRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE);
            if (account.getStatus() == AccountStatus.ACTIVE && activeAdminCount <= 1) {
                throw new AppException(ErrorCode.LAST_ADMIN_PROTECTION);
            }
        }

        account.setRole(request.getRole());
        // BR-U03: Token version increment to invalidate old tokens
        account.setTokenVersion(account.getTokenVersion() + 1);

        accountRepository.save(account);

        return StaffResponse.fromEntity(staff);
    }

    @Override
    @Transactional
    public StaffStatusResponse updateStaffStatus(Long id, UpdateStaffStatusRequest request, Long currentAdminAccountId) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        Account account = staff.getAccount();
        if (account == null) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }

        // BR-U01: Self modification forbidden
        if (currentAdminAccountId != null && account.getId().equals(currentAdminAccountId)) {
            throw new AppException(ErrorCode.SELF_MODIFICATION_FORBIDDEN);
        }

        // BR-U02: Last admin protection when locking
        if (request.getStatus() == AccountStatus.LOCKED && account.getRole() == Role.ADMIN) {
            long activeAdminCount = accountRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE);
            if (account.getStatus() == AccountStatus.ACTIVE && activeAdminCount <= 1) {
                throw new AppException(ErrorCode.LAST_ADMIN_PROTECTION);
            }
        }

        account.setStatus(request.getStatus());

        if (request.getStatus() == AccountStatus.LOCKED) {
            account.setLockedReason(request.getReason());
            // BR-U03: Invalidate all sessions immediately
            account.setTokenVersion(account.getTokenVersion() + 1);
        } else {
            account.setLockedReason(null);
            account.setFailedLoginCount(0);
            account.setLoginLockedUntil(null);
        }

        accountRepository.save(account);

        return StaffStatusResponse.builder()
                .id(staff.getId())
                .accountId(account.getId())
                .status(account.getStatus())
                .reason(account.getLockedReason())
                .build();
    }

    @Override
    @Transactional
    public ResetStaffPasswordResponse resetStaffPassword(Long id, Long currentAdminAccountId) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        Account account = staff.getAccount();
        if (account == null) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }

        // BR-U01: Self modification forbidden
        if (currentAdminAccountId != null && account.getId().equals(currentAdminAccountId)) {
            throw new AppException(ErrorCode.SELF_MODIFICATION_FORBIDDEN);
        }

        String temporaryPassword = SecurePasswordGenerator.generateTemporaryPassword();
        account.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        account.setStatus(AccountStatus.UNVERIFIED);
        account.setTokenVersion(account.getTokenVersion() + 1);
        account.setFailedLoginCount(0);
        account.setLoginLockedUntil(null);

        accountRepository.save(account);

        return ResetStaffPasswordResponse.builder()
                .phone(account.getUsername())
                .temporaryPassword(temporaryPassword)
                .build();
    }
}

