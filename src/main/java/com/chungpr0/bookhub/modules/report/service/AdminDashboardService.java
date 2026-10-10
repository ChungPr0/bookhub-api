package com.chungpr0.bookhub.modules.report.service;

import com.chungpr0.bookhub.modules.report.dto.response.DashboardRealtimeResponse;
import com.chungpr0.bookhub.modules.report.dto.response.DashboardSummaryResponse;

public interface AdminDashboardService {

    DashboardSummaryResponse getDashboardSummary(String period);

    DashboardRealtimeResponse getDashboardRealtime();
}

