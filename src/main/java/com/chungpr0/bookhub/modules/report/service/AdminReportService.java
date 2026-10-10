package com.chungpr0.bookhub.modules.report.service;

import com.chungpr0.bookhub.modules.report.dto.request.CustomerReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.ExportReportRequest;
import com.chungpr0.bookhub.modules.report.dto.request.InventoryReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.RevenueReportFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.request.TopBooksFilterRequest;
import com.chungpr0.bookhub.modules.report.dto.response.CustomerReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.InventoryReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.RevenueReportResponse;
import com.chungpr0.bookhub.modules.report.dto.response.TopBookReportResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminReportService {

    RevenueReportResponse getRevenueReport(RevenueReportFilterRequest request);

    List<TopBookReportResponse> getTopBooksReport(TopBooksFilterRequest request);

    InventoryReportResponse getInventoryReport(InventoryReportFilterRequest request, Pageable pageable);

    CustomerReportResponse getCustomerReport(CustomerReportFilterRequest request);

    byte[] exportRevenueReport(ExportReportRequest request);
}

