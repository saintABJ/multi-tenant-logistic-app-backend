package com.example.multi_tenant_logistic_app.service;

import com.example.multi_tenant_logistic_app.domain.entity.LogisticsCompany;
import com.example.multi_tenant_logistic_app.domain.entity.User;
import com.example.multi_tenant_logistic_app.domain.enumeration.AccountType;
import com.example.multi_tenant_logistic_app.dto.responses.LoginResponse;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCompanyRequest;
import com.example.multi_tenant_logistic_app.dto.requests.LoginRequest;
import com.example.multi_tenant_logistic_app.dto.requests.RegisterCustomerRequest;
import com.example.multi_tenant_logistic_app.exception.UserAlreadyExistsException;
import com.example.multi_tenant_logistic_app.repository.LogisticsCompanyRepository;
import com.example.multi_tenant_logistic_app.repository.UserRepository;
import com.example.multi_tenant_logistic_app.security.JwtTokenProvider;
import com.example.multi_tenant_logistic_app.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LogisticsCompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;

    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        tokenProvider = new JwtTokenProvider();
        org.springframework.test.util.ReflectionTestUtils.setField(tokenProvider, "secretKey", "mySecretKeyMySecretKeyMySecretKeyMySecretKey");
        org.springframework.test.util.ReflectionTestUtils.setField(tokenProvider, "validityInMilliseconds", 3600000L);
        tokenProvider.init();
        
        authService = new AuthServiceImpl(userRepository, companyRepository, passwordEncoder, tokenProvider);
    }

    @Test
    public void testRegisterCustomer_Success() {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");
        request.setPhone("1234567890");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerCustomer(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterCustomer_UserAlreadyExists() {
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerCustomer(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterCompany_Success() {
        RegisterCompanyRequest request = new RegisterCompanyRequest();
        request.setCompanyName("Test Company");
        request.setCompanyEmail("company@example.com");
        request.setAdminPassword("password");
        request.setAdminName("Admin");
        request.setAdminEmail("admin@example.com");
        request.setAdminPhone("1234567890");
        request.setCompanyPhone("0987654321");
        request.setRcNumber("RC123");

        when(companyRepository.save(any(LogisticsCompany.class))).thenAnswer(invocation -> {
            LogisticsCompany c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });
        when(passwordEncoder.encode(request.getAdminPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.registerCompany(request);

        verify(companyRepository, times(1)).save(any(LogisticsCompany.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setAccountType(AccountType.CUSTOMER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        LoginResponse response = authService.login(request);

        assertNotNull(response.getToken());
    }

    @Test
    public void testLogin_BadCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }
}
