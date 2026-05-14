package com.example.multi_tenant_logistic_app.service.impl;

import com.example.multi_tenant_logistic_app.domain.entity.LogisticsCompany;
import com.example.multi_tenant_logistic_app.domain.entity.User;
import com.example.multi_tenant_logistic_app.domain.enumeration.AccountType;
import com.example.multi_tenant_logistic_app.domain.enumeration.CompanyStatus;
import com.example.multi_tenant_logistic_app.domain.enumeration.SubscriptionPlan;
import com.example.multi_tenant_logistic_app.dto.requests.LoginRequest;
import com.example.multi_tenant_logistic_app.dto.responses.LoginResponse;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCompanyRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCustomerRequest;
import com.example.multi_tenant_logistic_app.exception.InvalidCredentialsException;
import com.example.multi_tenant_logistic_app.exception.UserAlreadyExistsException;
import com.example.multi_tenant_logistic_app.repository.LogisticsCompanyRepository;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.security.JwtTokenProvider;
import com.example.multi_tenant_logistic_app.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final LogisticsCompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           LogisticsCompanyRepository companyRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public void registerCustomer(RegisterCustomerRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .accountType(AccountType.CUSTOMER)
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void registerCompany(RegisterCompanyRequest request) {
        if (companyRepository.findByEmail(request.getCompanyEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Company email already in use");
        }
        if (userRepository.findByEmail(request.getAdminEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Admin email already in use");
        }

        LogisticsCompany company = LogisticsCompany.builder()
                .name(request.getCompanyName())
                .email(request.getCompanyEmail())
                .phone(request.getCompanyPhone())
                .rcNumber(request.getRcNumber())
                .subscriptionPlan(SubscriptionPlan.BASIC) // Default plan
                .status(CompanyStatus.PENDING)
                .build();

        company = companyRepository.save(company);

        User admin = User.builder()
                .name(request.getAdminName())
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .phone(request.getAdminPhone())
                .accountType(AccountType.COMPANY_ADMIN)
                .company(company)
                .build();

        userRepository.save(admin);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        Long companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        String token = jwtTokenProvider.createToken(user.getEmail(), companyId, user.getAccountType().name());

        return new LoginResponse(token, user.getAccountType().name(), companyId);
    }
}
