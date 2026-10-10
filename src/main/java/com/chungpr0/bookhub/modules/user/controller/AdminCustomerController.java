package com.chungpr0.bookhub.modules.user.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
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
import com.chungpr0.bookhub.modules.user.service.AdminCustomerService;
import com.chungpr0.bookhub.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@Tag(name = "8.1 Admin Customers", description = "APIs quản lý khách hàng, hồ sơ, khóa tài khoản và điều chỉnh điểm thưởng")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(
            summary = "ACU-01: Danh sách khách hàng (Lọc & Phân trang)",
            description = "Lấy danh sách khách hàng hỗ trợ tìm kiếm theo từ khóa, lọc theo trạng thái, hạng thành viên, khoảng ngày đăng ký và chi tiêu",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<CustomerSummaryResponse>>> getCustomers(
            @ModelAttribute CustomerFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<CustomerSummaryResponse> response = adminCustomerService.getCustomers(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(
            summary = "ACU-02: Chi tiết khách hàng",
            description = "Xem thông tin chi tiết của khách hàng bao gồm hồ sơ, danh sách địa chỉ nhận hàng, thống kê số lượng đơn và tiến trình thăng hạng",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CustomerDetailResponse>> getCustomerDetail(
            @PathVariable Long id
    ) {
        CustomerDetailResponse response = adminCustomerService.getCustomerDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết thông tin khách hàng thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ACU-03: Cập nhật thông tin khách hàng",
            description = "Cập nhật họ tên, email, giới tính, ngày sinh của khách hàng hỗ trợ CSKH qua tổng đài. Không cho phép sửa số điện thoại định danh",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CustomerSummaryResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        CustomerSummaryResponse response = adminCustomerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin khách hàng thành công", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ACU-04: Khóa / Mở khóa tài khoản khách hàng",
            description = "Thay đổi trạng thái tài khoản khách hàng (ACTIVE / LOCKED). Khi khóa bắt buộc nhập lý do và hệ thống tự động thu hồi toàn bộ phiên đăng nhập hiện tại",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CustomerStatusResponse>> updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerStatusRequest request
    ) {
        CustomerStatusResponse response = adminCustomerService.updateCustomerStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tài khoản khách hàng thành công", response));
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(
            summary = "ACU-05: Lịch sử đơn hàng của khách hàng",
            description = "Xem danh sách các đơn hàng đã đặt của một khách hàng cụ thể kèm bộ lọc theo trạng thái đơn hàng và phân trang",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<CustomerOrderHistoryResponse>>> getCustomerOrders(
            @PathVariable Long id,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<CustomerOrderHistoryResponse> response = adminCustomerService.getCustomerOrders(id, status, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử đơn hàng của khách hàng thành công", response));
    }

    @PostMapping("/{id}/points/adjustments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
            summary = "ACU-06: Điều chỉnh điểm thưởng khách hàng",
            description = "Điều chỉnh cộng bù hoặc trừ điểm thưởng của khách hàng (dương = cộng bù, âm = trừ phạt). Yêu cầu nhập lý do chi tiết",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PointAdjustmentResponse>> adjustPoints(
            @PathVariable Long id,
            @Valid @RequestBody AdjustPointsRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long adminAccountId = (principal != null) ? principal.getAccountId() : null;
        PointAdjustmentResponse response = adminCustomerService.adjustPoints(id, request, adminAccountId);
        return ResponseEntity.ok(ApiResponse.success("Điều chỉnh điểm thưởng khách hàng thành công", response));
    }

    @GetMapping("/{id}/points/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    @Operation(
            summary = "ACU-07: Lịch sử biến động điểm thưởng của khách",
            description = "Xem lịch sử tất cả các giao dịch tích lũy, đổi điểm, hoàn điểm hoặc điều chỉnh thủ công của khách hàng",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<PageResponse<PointTransactionResponse>>> getCustomerPointsHistory(
            @PathVariable Long id,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<PointTransactionResponse> response = adminCustomerService.getCustomerPointsHistory(id, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử điểm thưởng của khách hàng thành công", response));
    }
}

