package com.chungpr0.bookhub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "bookhub.cors")
public class CorsProperties {

    /**
     * Allowed origin patterns for CORS requests (injected via CORS_ALLOWED_ORIGINS).
     */
    private List<String> allowedOrigins = List.of(
            "http://localhost:3000",
            "http://localhost:5173"
    );

    /**
     * Public backend server URL for OpenAPI / Swagger UI (injected via BACKEND_URL).
     */
    private String backendUrl = "http://localhost:8080";
}
