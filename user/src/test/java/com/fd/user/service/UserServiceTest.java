package com.fd.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.fd.user.dto.AuthResponse;
import com.fd.user.dto.LoginRequest;
import com.fd.user.dto.RegisterRequest;
import com.fd.user.entity.Role;
import com.fd.user.entity.User;
import com.fd.user.repository.AddressRepository;
import com.fd.user.repository.UserRepository;
import com.fd.user.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_shouldSaveUserAndSendWelcomeEmail() {
        RegisterRequest request = new RegisterRequest("new.user@example.com", "plain-password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(request.getEmail());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.register(request);

        verify(userRepository).save(any(User.class));
        verify(emailService).sendWelcomeEmail(request.getEmail());
    }

    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing.user@example.com", "plain-password");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User already exists");

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any());
    }

    @Test
    void login_shouldReturnTokenAndRoleForValidCredentials() {
        LoginRequest request = new LoginRequest("valid.user@example.com", "plain-password");

        User user = new User();
        user.setId(10L);
        user.setEmail(request.getEmail());
        user.setPassword("encoded-password");
        user.setRole(Role.user);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = userService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.user.name());
    }
}
