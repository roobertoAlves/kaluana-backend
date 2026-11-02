package br.edu.ifsp.kaluana.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** app.cors.*: origens do front liberadas no CORS, separadas por vírgula (sem "*"). */
@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(@NotBlank String allowedOrigin) {
}
