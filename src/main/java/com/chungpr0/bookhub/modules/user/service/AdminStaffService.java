package com.chungpr0.bookhub.modules.user.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.user.dto.request.CreateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.StaffFilterRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRoleRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffStatusRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CreateStaffResponse;
import com.chungpr0.bookhub.modules.user.dto.response.ResetStaffPasswordResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffStatusResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffResponse;
import org.springframework.data.domain.Pageable;

public interface AdminStaffService {

    PageResponse<StaffResponse> getStaffs(StaffFilterRequest filter, Pageable pageable);

    StaffResponse getStaffDetail(Long id);

    CreateStaffResponse createStaff(CreateStaffRequest request, Long adminAccountId);

    StaffResponse updateStaff(Long id, UpdateStaffRequest request);

    StaffResponse updateStaffRole(Long id, UpdateStaffRoleRequest request, Long currentAdminAccountId);

    StaffStatusResponse updateStaffStatus(Long id, UpdateStaffStatusRequest request, Long currentAdminAccountId);

    ResetStaffPasswordResponse resetStaffPassword(Long id, Long currentAdminAccountId);
}

