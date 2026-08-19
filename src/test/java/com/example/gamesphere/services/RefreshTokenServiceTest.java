package com.example.gamesphere.services;

import com.example.gamesphere.config.JwtConfig;
import com.example.gamesphere.entity.RefreshToken;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.InvalidCredentialsException;
import com.example.gamesphere.repository.RefreshTokenRepository;
import com.example.gamesphere.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest extends ServiceTestSupport {

    @Mock RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setRefreshExpiration(86_400_000L);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, config);
        org.mockito.Mockito.lenient()
                .when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void issueReturnsOpaqueTokenButStoresOnlyItsHash() {
        User user = user(1L, "user@mail.com");

        String rawToken = refreshTokenService.issue(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(rawToken).isNotBlank().hasSize(64);
        assertThat(captor.getValue().getTokenHash()).hasSize(64).isNotEqualTo(rawToken);
        assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    void rotateRevokesOldTokenAndReturnsReplacement() {
        User user = user(1L, "user@mail.com");
        RefreshToken old = RefreshToken.builder()
                .user(user)
                .tokenHash("old-hash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(old));

        RefreshTokenService.RotatedToken rotated = refreshTokenService.rotate("old-raw-token");

        assertThat(rotated.user()).isSameAs(user);
        assertThat(rotated.rawToken()).isNotBlank().isNotEqualTo("old-raw-token");
        assertThat(old.getRevokedAt()).isNotNull();
        assertThat(old.getReplacedByTokenHash()).hasSize(64);
    }

    @Test
    void reuseDetectionRevokesAllActiveSessions() {
        User user = user(1L, "user@mail.com");
        RefreshToken reused = RefreshToken.builder()
                .user(user)
                .tokenHash("old-hash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revokedAt(LocalDateTime.now())
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(reused));

        assertThatThrownBy(() -> refreshTokenService.rotate("reused-token"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("already used");
        verify(refreshTokenRepository).revokeAllActiveByUserId(
                org.mockito.ArgumentMatchers.eq(1L), any(LocalDateTime.class));
    }
}
