package com.example.gamesphere.integration;

import com.example.gamesphere.entity.Order;
import com.example.gamesphere.entity.User;
import com.example.gamesphere.enums.OrderStatus;
import com.example.gamesphere.repository.OrderRepository;
import com.example.gamesphere.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class SecurityAndAuthIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("gamesphere_security_test")
                    .withUsername("gamesphere")
                    .withPassword("gamesphere");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("app.mail.enabled", () -> "false");
        registry.add("app.stripe.enabled", () -> "false");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired OrderRepository orderRepository;

    @Test
    void publicCatalogIsAnonymousButOrdersRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/games/search").param("page", "0").param("size", "5"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/order/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void browserPreflightIsAnsweredBeforeAuthenticationKicksIn() throws Exception {
        mockMvc.perform(options("/api/order/my")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void refreshTokenRotatesAndOldTokenCannotBeReused() throws Exception {
        JsonNode registered = register("rotation-user", "rotation@mail.com");
        String firstRefreshToken = registered.path("data").path("refreshToken").asText();

        String refreshBody = objectMapper.writeValueAsString(
                java.util.Map.of("refreshToken", firstRefreshToken));
        String response = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String rotatedRefreshToken = objectMapper.readTree(response)
                .path("data").path("refreshToken").asText();
        assertThat(rotatedRefreshToken).isNotBlank().isNotEqualTo(firstRefreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void orderDetailsAreVisibleOnlyToOwner() throws Exception {
        JsonNode ownerAuth = register("order-owner", "owner@mail.com");
        JsonNode strangerAuth = register("order-stranger", "stranger@mail.com");
        User owner = userRepository.findByEmail("owner@mail.com").orElseThrow();

        Order order = Order.builder()
                .user(owner)
                .orderNumber("GS-INTEGRATION-1")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("19.99"))
                .build();
        order = orderRepository.saveAndFlush(order);

        mockMvc.perform(get("/api/order/{id}", order.getId())
                        .header("Authorization", "Bearer "
                                + strangerAuth.path("data").path("accessToken").asText()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/order/{id}", order.getId())
                        .header("Authorization", "Bearer "
                                + ownerAuth.path("data").path("accessToken").asText()))
                .andExpect(status().isOk());
    }

    private JsonNode register(String username, String email) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "username", username,
                "email", email,
                "password", "Secure123!",
                "firstName", "Test",
                "lastName", "User"));
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }
}
