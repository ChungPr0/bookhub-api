package com.chungpr0.bookhub.modules.report.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.report.dto.response.DashboardRealtimeResponse;
import com.chungpr0.bookhub.modules.report.dto.response.DashboardSummaryResponse;
import com.chungpr0.bookhub.modules.report.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Tag(name = "8.3 Admin Dashboard", description = "APIs tổng quan các chỉ số kinh doanh và hoạt động vận hành thời gian thực")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    @Operation(
            summary = "RPT-01: Dashboard tổng quan chỉ số kinh doanh",
            description = "Lấy các chỉ số then chốt (doanh thu, đơn hàng, khách mới, AOV, cảnh báo khẩn cấp, biểu đồ và top sách) theo chu kỳ TODAY, LAST_7_DAYS, LAST_30_DAYS, THIS_MONTH",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(
            @RequestParam(defaultValue = "TODAY") String period
    ) {
        DashboardSummaryResponse response = adminDashboardService.getDashboardSummary(period);
        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu dashboard tổng quan thành công", response));
    }

    @GetMapping("/realtime")
    @Operation(
            summary = "RPT-02: Chỉ số hoạt động thời gian thực trong ngày",
            description = "Cập nhật tức thời tình hình vận hành trong ngày: doanh thu, đơn hoàn tất, tỷ lệ hủy, biểu đồ theo từng khung giờ và hàng đợi khẩn cấp",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<DashboardRealtimeResponse>> getDashboardRealtime() {
        DashboardRealtimeResponse response = adminDashboardService.getDashboardRealtime();
        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu vận hành thời gian thực thành công", response));
    }
}

