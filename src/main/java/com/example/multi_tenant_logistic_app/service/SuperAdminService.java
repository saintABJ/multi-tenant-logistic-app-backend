package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.dto.responses.CompanyStatsResponse;
import com.example.multi_tenant_logistic_app.dto.responses.PlatformStatsResponse;

import java.util.List;

public interface SuperAdminService {
    void approveCompany(Long companyId);
    void suspendCompany(Long companyId);
    List<CompanyStatsResponse> getCompaniesWithStats();
    PlatformStatsResponse getPlatformStats();
}
