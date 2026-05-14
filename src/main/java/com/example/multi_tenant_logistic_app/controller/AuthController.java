package com.example.multi_tenant_logistic_app.controller;

import com.example.multi_tenant_logistic_app.dto.requests.LoginRequest;
import com.example.multi_tenant_logistic_app.dto.responses.LoginResponse;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCompanyRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCustomerRequest;
import com.example.multi_tenant_logistic_app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        authService.registerCustomer(request);
        return ResponseEntity.ok("Customer registered successfully");
    }

    @PostMapping("/register/company")
    public ResponseEntity<String> registerCompany(@Valid @RequestBody RegisterCompanyRequest request) {
        authService.registerCompany(request);
        return ResponseEntity.ok("Company registered successfully. Status is PENDING.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
