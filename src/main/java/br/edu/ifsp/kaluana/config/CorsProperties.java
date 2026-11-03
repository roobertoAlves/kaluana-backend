package br.edu.ifsp.kaluana.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(@NotBlank String allowedOrigin) {
}
