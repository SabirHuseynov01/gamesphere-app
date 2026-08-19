package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.LoginRequest;
import com.example.gamesphere.dto.request.RefreshTokenRequest;
import com.example.gamesphere.dto.request.RegisterRequest;
import com.example.gamesphere.dto.response.AuthResponse;
import com.example.gamesphere.entity.Role;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.InvalidCredentialsException;
import com.example.gamesphere.exception.UserAlreadyExistsException;
import com.example.gamesphere.repository.RoleRepository;
import com.example.gamesphere.repository.UserRepository;
import com.example.gamesphere.service.AuthService;
import com.example.gamesphere.service.EmailService;
import com.example.gamesphere.service.JwtService;
import com.example.gamesphere.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock
    EmailService emailService;
    @Mock RefreshTokenService refreshTokenService;
    @InjectMocks
    AuthService authService;

    @Test
    void registerCreatesUserWithDefaultRoleAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("sabir");
        request.setEmail("sabirhuseynov@mail.com");
        request.setPassword("Secret123!");
        request.setFirstName("Sabir");
        request.setLastName("Huseynov");

        Role role = Role.builder().name("ROLE_USER").build();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(request.getEmail())).thenReturn("jwt-token");
        when(refreshTokenService.issue(any(User.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(emailService).sendWelcomeEmail(userCaptor.getValue());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(userCaptor.getValue().getRoles()).containsExactly(role);
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void registerRejectsExistingEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("used@mail.com");
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void loginConvertsAuthenticationFailureToDomainException() {
        LoginRequest request = new LoginRequest("user@mail.com", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(mock(AuthenticationException.class));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password.");
    }

    @Test
    void refreshRotatesRefreshToken() {
        User user = User.builder().username("sabir").email("user@mail.com").build();
        when(refreshTokenService.rotate("Bearer old-token"))
                .thenReturn(new RefreshTokenService.RotatedToken(user, "rotated-token"));
        when(jwtService.generateToken("user@mail.com")).thenReturn("new-token");

        AuthResponse response = authService.refreshToken(new RefreshTokenRequest("Bearer old-token"));

        assertThat(response.getAccessToken()).isEqualTo("new-token");
        assertThat(response.getRefreshToken()).isEqualTo("rotated-token");
    }
}
