package com.example.gamesphere.service;

import com.example.gamesphere.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private static final long DEFAULT_EXPIRATION_MS = 86_400_000L; // 24 hours
    private static final long MIN_REASONABLE_EXPIRATION_MS = 60_000L;

    private final JwtConfig jwtConfig;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    public String generateToken(String username) {
        long expirationMillis = getExpirationMillis();
        Date issuedAt = new Date();
        Date expiresAt = new Date(System.currentTimeMillis() + expirationMillis);

        log.debug("Generating JWT for {}. issuedAt={}, expiresAt={}, expirationMillis={}",
                username, issuedAt, expiresAt, expirationMillis);

        return Jwts.builder()
                .subject(username)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    private long getExpirationMillis() {
        long configuredExpiration = jwtConfig.getAccessExpiration();

        if (configuredExpiration < MIN_REASONABLE_EXPIRATION_MS) {
            log.warn("JWT expiration is configured as {} ms. Using default {} ms instead.",
                    configuredExpiration, DEFAULT_EXPIRATION_MS);
            return DEFAULT_EXPIRATION_MS;
        }

        return configuredExpiration;
    }
}

