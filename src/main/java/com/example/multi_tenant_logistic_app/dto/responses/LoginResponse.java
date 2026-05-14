package com.example.multi_tenant_logistic_app.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String accountType;
    private Long companyId;
}
