package com.example.gamesphere.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    private final WebConfig webConfig =
            new WebConfig("/tmp/gamesphere-uploads", "http://localhost:3000, http://localhost:5173");

    @Test
    void apiRequestsResolveTheConfiguredCorsPolicy() {
        CorsConfiguration configuration = resolveFor("/api/order/my");

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:3000", "http://localhost:5173");
        assertThat(configuration.getAllowedMethods())
                .contains("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void uploadedFilesAreCorsEnabledSoTheFrontendCanRenderThem() {
        assertThat(resolveFor("/uploads/products/cover.png")).isNotNull();
    }

    @Test
    void pathsOutsideTheApiKeepNoCorsPolicy() {
        assertThat(resolveFor("/actuator/health")).isNull();
    }

    private CorsConfiguration resolveFor(String path) {
        CorsConfigurationSource source = webConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest(HttpMethod.OPTIONS.name(), path);
        request.addHeader(HttpHeaders.ORIGIN, "http://localhost:5173");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name());
        return source.getCorsConfiguration(request);
    }
}
