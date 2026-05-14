package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.LogisticsCompany;
import com.example.multi_tenant_logistic_app.domain.enumeration.CompanyStatus;
import com.example.multi_tenant_logistic_app.dto.responses.CompanyStatsResponse;
import com.example.multi_tenant_logistic_app.dto.responses.PlatformStatsResponse;
import com.example.multi_tenant_logistic_app.repository.LogisticsCompanyRepository;
import com.example.multi_tenant_logistic_app.repository.ShipmentRepository;
import com.example.multi_tenant_logistic_app.service.impl.SuperAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SuperAdminServiceTest {

    @Mock
    private LogisticsCompanyRepository companyRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private SuperAdminServiceImpl superAdminService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testApproveCompany_Success() {
        Long companyId = 1L;
        LogisticsCompany company = new LogisticsCompany();
        company.setId(companyId);
        company.setStatus(CompanyStatus.PENDING);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        superAdminService.approveCompany(companyId);

        assertEquals(CompanyStatus.ACTIVE, company.getStatus());
        verify(companyRepository, times(1)).save(company);
    }

    @Test
    public void testApproveCompany_NotFound() {
        Long companyId = 1L;

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> superAdminService.approveCompany(companyId));
    }

    @Test
    public void testSuspendCompany_Success() {
        Long companyId = 1L;
        LogisticsCompany company = new LogisticsCompany();
        company.setId(companyId);
        company.setStatus(CompanyStatus.ACTIVE);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        superAdminService.suspendCompany(companyId);

        assertEquals(CompanyStatus.SUSPENDED, company.getStatus());
        verify(companyRepository, times(1)).save(company);
    }

    @Test
    public void testGetCompaniesWithStats() {
        List<CompanyStatsResponse> list = new ArrayList<>();
        when(shipmentRepository.getCompaniesWithStats()).thenReturn(list);

        List<CompanyStatsResponse> result = superAdminService.getCompaniesWithStats();

        assertEquals(list, result);
    }

    @Test
    public void testGetPlatformStats() {
        when(shipmentRepository.countShipmentsToday(any())).thenReturn(5L);
        when(shipmentRepository.countDeliveredThisMonth(any())).thenReturn(10L);
        when(shipmentRepository.getAverageDeliveryTimeHours()).thenReturn(2.5);

        PlatformStatsResponse response = superAdminService.getPlatformStats();

        assertNotNull(response);
        assertEquals(5L, response.getTotalShipmentsToday());
        assertEquals(10L, response.getTotalDeliveredThisMonth());
        assertEquals(2.5, response.getAverageDeliveryTimeHours());
    }
}
