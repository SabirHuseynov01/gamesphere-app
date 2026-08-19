package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.LoginRequest;
import com.example.gamesphere.dto.request.RefreshTokenRequest;
import com.example.gamesphere.dto.request.RegisterRequest;
import com.example.gamesphere.dto.response.AuthResponse;
import com.example.gamesphere.entity.Role;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.exception.InvalidCredentialsException;
import com.example.gamesphere.exception.ResourceNotFoundException;
import com.example.gamesphere.exception.UserAlreadyExistsException;
import com.example.gamesphere.repository.RoleRepository;
import com.example.gamesphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEFAULT_USER_ROLE = "ROLE_USER";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("This email is already registered.");
        }
        if (userRepository.existsByUsername(request.getUsername())){
            throw new UserAlreadyExistsException("This username is already available.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        user.getRoles().add(getOrCreateDefaultRole());
        user = userRepository.save(user);
        emailService.sendWelcomeEmail(user);

        String accessToken = jwtService.generateToken(request.getEmail());
        String refreshToken = refreshTokenService.issue(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("User not found"));

        String accessToken = jwtService.generateToken(request.getEmail());
        String refreshToken = refreshTokenService.issue(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenService.RotatedToken rotated = refreshTokenService.rotate(request.getRefreshToken());
        User user = rotated.user();
        String newAccessToken = jwtService.generateToken(user.getEmail());
        return buildAuthResponse(user, newAccessToken, rotated.rawToken());
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.getRefreshToken());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private Role getOrCreateDefaultRole() {
        return roleRepository.findByName(DEFAULT_USER_ROLE)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(DEFAULT_USER_ROLE)
                                .build()));
    }
}
