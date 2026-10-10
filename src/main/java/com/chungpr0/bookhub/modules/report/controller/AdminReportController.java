package com.chungpr0.bookhub.modules.report.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.config.OpenApiConfig;
import com.chungpr0.bookhub.modules.report.dto.request.CustomerReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.ExportReportRequest;
import com.chungpr0.bookhub.modules.report.dto.request.InventoryReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.RevenueReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.TopBooksFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.response.CustomerReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.InventoryReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.RevenueReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.TopBookReportResponse;
import com.chungpr0.bookhub.modules.report.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Tag(name = "8.4 Admin Reports", description = "APIs báo cáo phân tích tài chính, bán chạy, tình trạng tồn kho, khách hàng và xuất file")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/revenue")
    @Operation(
            summary = "RPT-03: Báo cáo chi tiết Doanh thu & Lợi nhuận",
            description = "Phân tích chi tiết doanh thu, chiết khấu, giá vốn FIFO và lợi nhuận gộp theo mốc thời gian tùy chọn với các cách nhóm dữ liệu theo ngày, tuần hoặc tháng",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<RevenueReportResponse>> getRevenueReport(
            @Valid @ModelAttribute RevenueReportFilterRequest request
    ) {
        RevenueReportResponse response = adminReportService.getRevenueReport(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh thu & lợi nhuận thành công", response));
    }

    @GetMapping("/top-books")
    @Operation(
            summary = "RPT-04: Báo cáo Sách bán chạy nhất",
            description = "Bảng xếp hạng các đầu sách bán chạy nhất theo số lượng, doanh thu hoặc lợi nhuận gộp sinh ra trong kỳ",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<List<TopBookReportResponse>>> getTopBooksReport(
            @Valid @ModelAttribute TopBooksFilterRequest request
    ) {
        List<TopBookReportResponse> response = adminReportService.getTopBooksReport(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy bảng xếp hạng sách bán chạy thành công", response));
    }

    @GetMapping("/inventory")
    @Operation(
            summary = "RPT-05: Báo cáo Giá trị & Tình trạng tồn kho",
            description = "Báo cáo tổng giá trị vốn đọng trong kho theo từng lô (FIFO), định giá bán lẻ tương ứng và phát hiện các tựa sách không có phát sinh giao dịch trong 90 ngày (Hàng ế)",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<InventoryReportResponse>> getInventoryReport(
            @ModelAttribute InventoryReportFilterRequest request,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        InventoryReportResponse response = adminReportService.getInventoryReport(request, pageable);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo giá trị tồn kho thành công", response));
    }

    @GetMapping("/customers")
    @Operation(
            summary = "RPT-06: Báo cáo Phân khúc & Tỷ lệ quay lại của khách",
            description = "Thống kê phân bổ hạng thành viên, biểu đồ tăng trưởng khách hàng mới, top khách hàng chi tiêu nhiều nhất và tỷ lệ mua lại",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<ApiResponse<CustomerReportResponse>> getCustomerReport(
            @Valid @ModelAttribute CustomerReportFilterRequest request
    ) {
        CustomerReportResponse response = adminReportService.getCustomerReport(request);
        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo phân tích khách hàng thành công", response));
    }

    @GetMapping("/revenue/export")
    @Operation(
            summary = "RPT-07: Xuất file báo cáo tài chính (Excel/CSV)",
            description = "Tải về file báo cáo chi tiết doanh thu và lợi nhuận định dạng CSV hoặc bảng tính Excel",
            security = @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    )
    public ResponseEntity<byte[]> exportRevenueReport(
            @Valid @ModelAttribute ExportReportRequest request
    ) {
        byte[] fileBytes = adminReportService.exportRevenueReport(request);

        String fromStr = request.getFrom().format(DateTimeFormatter.BASIC_ISO_DATE);
        String toStr = request.getTo().format(DateTimeFormatter.BASIC_ISO_DATE);
        String extension = "XLSX".equalsIgnoreCase(request.getFormat()) ? "xlsx" : "csv";
        String filename = String.format("bookhub-revenue-%s-%s.%s", fromStr, toStr, extension);

        MediaType mediaType = "XLSX".equalsIgnoreCase(request.getFormat())
                ? MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.parseMediaType("text/csv; charset=UTF-8");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentType(mediaType)
                .body(fileBytes);
    }
}

