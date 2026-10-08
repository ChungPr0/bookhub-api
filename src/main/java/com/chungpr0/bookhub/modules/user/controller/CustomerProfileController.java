package com.chungpr0.bookhub.modules.user.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.enums.PointTransactionType;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateProfileRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CustomerProfileResponse;
import com.chungpr0.bookhub.modules.user.dto.response.PointsOverviewResponse;
import com.chungpr0.bookhub.modules.user.service.CustomerProfileService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "2. Customer Account & Addresses", description = "Quản lý thông tin hồ sơ cá nhân, hạng thành viên và điểm thưởng khách hàng")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @Operation(
            summary = "PRF-01: Xem hồ sơ cá nhân & tiến trình thăng hạng",
            description = "Lấy thông tin tài khoản cá nhân, thông tin liên hệ, tổng chi tiêu và chi tiết tiến trình thăng hạng thành viên.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        CustomerProfileResponse response = customerProfileService.getProfile(principal.getAccountId());
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin hồ sơ cá nhân thành công", response));
    }

    @Operation(
            summary = "PRF-02: Cập nhật hồ sơ cá nhân",
            description = "Cập nhật họ tên, email, giới tính, ngày sinh và ảnh đại diện CDN. Không cho phép thay đổi số điện thoại và điểm thưởng.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        CustomerProfileResponse response = customerProfileService.updateProfile(principal.getAccountId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật hồ sơ cá nhân thành công", response));
    }

    @Operation(
            summary = "PRF-03: Xem điểm thưởng & lịch sử tích/dùng điểm",
            description = "Hiển thị tổng quan điểm thưởng khả dụng, hạng thành viên, hệ số tích điểm và lịch sử biến động điểm hỗ trợ lọc type và phân trang.",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    @GetMapping("/points")
    public ResponseEntity<ApiResponse<PointsOverviewResponse>> getPoints(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) PointTransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        int validatedSize = Math.max(1, Math.min(50, size));
        int validatedPage = Math.max(0, page);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);

        PointsOverviewResponse response = customerProfileService.getPoints(principal.getAccountId(), type, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin điểm thưởng thành công", response));
    }
}

