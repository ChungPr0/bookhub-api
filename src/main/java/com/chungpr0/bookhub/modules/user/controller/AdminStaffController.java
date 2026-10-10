package com.chungpr0.bookhub.modules.user.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.user.dto.request.CreateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.StaffFilterRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffRoleRequest;
import com.chungpr0.bookhub.modules.user.dto.request.UpdateStaffStatusRequest;
import com.chungpr0.bookhub.modules.user.dto.response.CreateStaffResponse;
import com.chungpr0.bookhub.modules.user.dto.response.ResetStaffPasswordResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffResponse;
import com.chungpr0.bookhub.modules.user.dto.response.StaffStatusResponse;
import com.chungpr0.bookhub.modules.user.service.AdminStaffService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/staffs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "8.2 Admin Staffs", description = "APIs quản lý nhân sự nội bộ, phân quyền và cấp phát mật khẩu tạm (Chỉ dành riêng cho ADMIN)")
public class AdminStaffController {

    private final AdminStaffService adminStaffService;

    @GetMapping
    @Operation(
            summary = "AST-01: Danh sách nhân viên nội bộ",
            description = "Lấy danh sách tài khoản nhân viên nội bộ kèm tìm kiếm theo từ khóa, lọc theo vai trò và trạng thái",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<StaffResponse>>> getStaffs(
            @ModelAttribute StaffFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<StaffResponse> response = adminStaffService.getStaffs(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "AST-02: Chi tiết nhân viên",
            description = "Xem chi tiết hồ sơ tài khoản nhân viên bao gồm thông tin cá nhân, vai trò, trạng thái và lịch sử đăng nhập",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffDetail(
            @PathVariable Long id
    ) {
        StaffResponse response = adminStaffService.getStaffDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thông tin nhân viên thành công", response));
    }

    @PostMapping
    @Operation(
            summary = "AST-03: Tạo tài khoản nhân viên mới",
            description = "Tạo mới tài khoản nhân viên nội bộ. Hệ thống tự sinh mật khẩu ngẫu nhiên an toàn 12 ký tự, đặt trạng thái UNVERIFIED và trả về mật khẩu tạm một lần duy nhất",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CreateStaffResponse>> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long adminAccountId = (principal != null) ? principal.getAccountId() : null;
        CreateStaffResponse response = adminStaffService.createStaff(request, adminAccountId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo tài khoản nhân viên thành công. Vui lòng bàn giao mật khẩu tạm cho nhân sự", response));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "AST-04: Cập nhật thông tin nhân viên",
            description = "Cập nhật họ tên và email của nhân viên",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRequest request
    ) {
        StaffResponse response = adminStaffService.updateStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin nhân viên thành công", response));
    }

    @PatchMapping("/{id}/role")
    @Operation(
            summary = "AST-05: Thay đổi vai trò nhân sự",
            description = "Cập nhật vai trò (ADMIN, MANAGER, STAFF). Chặn tự đổi quyền của bản thân và bảo vệ Admin cuối cùng. Tự động thu hồi phiên đăng nhập cũ",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaffRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffRoleRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long currentAdminAccountId = (principal != null) ? principal.getAccountId() : null;
        StaffResponse response = adminStaffService.updateStaffRole(id, request, currentAdminAccountId);
        return ResponseEntity.ok(ApiResponse.success("Thay đổi vai trò nhân sự thành công", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "AST-06: Khóa / Mở khóa nhân viên",
            description = "Thay đổi trạng thái tài khoản nhân sự (ACTIVE / LOCKED). Chặn tự khóa chính mình và bảo vệ Admin cuối cùng. Khi khóa tự động thu hồi toàn bộ phiên đăng nhập",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<StaffStatusResponse>> updateStaffStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStaffStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long currentAdminAccountId = (principal != null) ? principal.getAccountId() : null;
        StaffStatusResponse response = adminStaffService.updateStaffStatus(id, request, currentAdminAccountId);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái nhân viên thành công", response));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(
            summary = "AST-07: Cấp lại mật khẩu tạm cho nhân viên",
            description = "Cấp lại mật khẩu tạm thời 12 ký tự cho nhân viên khi quên mật khẩu. Đặt lại trạng thái UNVERIFIED, thu hồi phiên cũ và chặn tự reset bản thân",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<ResetStaffPasswordResponse>> resetStaffPassword(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long currentAdminAccountId = (principal != null) ? principal.getAccountId() : null;
        ResetStaffPasswordResponse response = adminStaffService.resetStaffPassword(id, currentAdminAccountId);
        return ResponseEntity.ok(ApiResponse.success("Cấp lại mật khẩu tạm thời cho nhân viên thành công", response));
    }
}

