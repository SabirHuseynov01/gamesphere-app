package com.example.gamesphere.services;

import com.example.gamesphere.config.JwtConfig;
import com.example.gamesphere.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setSecret("gamesphere-test-secret-key-at-least-32-bytes-long");
        config.setAccessExpiration(3_600_000L);
        jwtService = new JwtService(config);
    }

    @Test
    void generatedTokenContainsUsernameAndIsValid() {
        String token = jwtService.generateToken("user@mail.com");

        assertThat(jwtService.extractUsername(token)).isEqualTo("user@mail.com");
        assertThat(jwtService.isTokenValid(token, "user@mail.com")).isTrue();
        assertThat(jwtService.isTokenValid(token, "other@mail.com")).isFalse();
    }

    @Test
    void malformedTokenIsInvalid() {
        assertThat(jwtService.isTokenValid("not-a-jwt", "user@mail.com")).isFalse();
    }
}
