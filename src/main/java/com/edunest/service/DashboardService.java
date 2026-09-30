package com.edunest.service;

import com.edunest.dto.dashboard.DashboardSummaryResponse;

public interface DashboardService {
    DashboardSummaryResponse getDashboardSummary(Integer tenantId);
}
