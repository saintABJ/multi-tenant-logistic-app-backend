package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.dto.requests.LoginRequest;
import com.example.multi_tenant_logistic_app.dto.responses.LoginResponse;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCompanyRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCustomerRequest;

public interface AuthService {
    void registerCustomer(RegisterCustomerRequest request);
    void registerCompany(RegisterCompanyRequest request);
    LoginResponse login(LoginRequest request);
}
