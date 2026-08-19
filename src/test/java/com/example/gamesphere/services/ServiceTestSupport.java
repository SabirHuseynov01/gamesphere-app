package com.example.gamesphere.services;

import com.example.gamesphere.entity.BaseEntity;
import com.example.gamesphere.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

abstract class ServiceTestSupport {

    protected void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    protected User user(Long id, String email) {
        User user = User.builder()
                .username("user-" + id)
                .email(email)
                .password("encoded-password")
                .build();
        user.setId(id);
        return user;
    }

    protected <T extends BaseEntity> T withId(T entity, Long id) {
        entity.setId(id);
        return entity;
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}
