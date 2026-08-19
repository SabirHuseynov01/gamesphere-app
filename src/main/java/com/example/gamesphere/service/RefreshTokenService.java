package com.example.gamesphere.service;

import com.example.gamesphere.config.JwtConfig;
import com.example.gamesphere.entity.RefreshToken;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.exception.InvalidCredentialsException;
import com.example.gamesphere.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Transactional
    public String issue(User user) {
        return createToken(user).rawToken();
    }

    @Transactional
    public RotatedToken rotate(String rawToken) {
        String normalized = normalize(rawToken);
        RefreshToken current = refreshTokenRepository.findByTokenHash(hash(normalized))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token."));

        if (current.isRevoked()) {
            refreshTokenRepository.revokeAllActiveByUserId(current.getUser().getId(), LocalDateTime.now());
            throw new InvalidCredentialsException("Refresh token was already used or revoked.");
        }

        if (current.isExpired()) {
            current.setRevokedAt(LocalDateTime.now());
            throw new InvalidCredentialsException("Refresh token has expired.");
        }

        GeneratedToken replacement = createToken(current.getUser());
        current.setRevokedAt(LocalDateTime.now());
        current.setReplacedByTokenHash(replacement.tokenHash());
        refreshTokenRepository.save(current);

        return new RotatedToken(current.getUser(), replacement.rawToken());
    }

    @Transactional
    public void revoke(String rawToken) {
        String normalized = normalize(rawToken);
        refreshTokenRepository.findByTokenHash(hash(normalized)).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    private GeneratedToken createToken(User user) {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String tokenHash = hash(rawToken);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusNanos(jwtConfig.getRefreshExpiration() * 1_000_000L))
                .build();
        refreshTokenRepository.save(token);
        return new GeneratedToken(rawToken, tokenHash);
    }

    private String normalize(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidCredentialsException("Refresh token is required.");
        }
        String normalized = token.trim();
        if (normalized.regionMatches(true, 0, "Bearer ", 0, 7)) {
            normalized = normalized.substring(7).trim();
        }
        if (normalized.isBlank()) {
            throw new InvalidCredentialsException("Refresh token is required.");
        }
        return normalized;
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private record GeneratedToken(String rawToken, String tokenHash) {
    }

    public record RotatedToken(User user, String rawToken) {
    }
}
