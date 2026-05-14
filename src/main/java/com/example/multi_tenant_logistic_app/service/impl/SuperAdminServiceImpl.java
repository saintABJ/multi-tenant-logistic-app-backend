package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.LogisticsCompany;
import com.example.multi_tenant_logistic_app.domain.enumeration.CompanyStatus;
import com.example.multi_tenant_logistic_app.dto.responses.CompanyStatsResponse;
import com.example.multi_tenant_logistic_app.dto.responses.PlatformStatsResponse;
import com.example.multi_tenant_logistic_app.repository.LogisticsCompanyRepository;
import com.example.multi_tenant_logistic_app.repository.ShipmentRepository;
import com.example.multi_tenant_logistic_app.service.SuperAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SuperAdminServiceImpl implements SuperAdminService {

    private final LogisticsCompanyRepository companyRepository;
    private final ShipmentRepository shipmentRepository;

    public SuperAdminServiceImpl(LogisticsCompanyRepository companyRepository, ShipmentRepository shipmentRepository) {
        this.companyRepository = companyRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public void approveCompany(Long companyId) {
        LogisticsCompany company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setStatus(CompanyStatus.ACTIVE);
        companyRepository.save(company);
    }

    @Override
    @Transactional
    public void suspendCompany(Long companyId) {
        LogisticsCompany company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        company.setStatus(CompanyStatus.SUSPENDED);
        companyRepository.save(company);
    }

    @Override
    public List<CompanyStatsResponse> getCompaniesWithStats() {
        return shipmentRepository.getCompaniesWithStats();
    }

    @Override
    public PlatformStatsResponse getPlatformStats() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIDNIGHT);

        Long totalShipmentsToday = shipmentRepository.countShipmentsToday(startOfDay);
        Long totalDeliveredThisMonth = shipmentRepository.countDeliveredThisMonth(startOfMonth);
        Double averageDeliveryTime = shipmentRepository.getAverageDeliveryTimeHours();

        return new PlatformStatsResponse(
                totalShipmentsToday != null ? totalShipmentsToday : 0L,
                totalDeliveredThisMonth != null ? totalDeliveredThisMonth : 0L,
                averageDeliveryTime != null ? averageDeliveryTime : 0.0
        );
    }
}
