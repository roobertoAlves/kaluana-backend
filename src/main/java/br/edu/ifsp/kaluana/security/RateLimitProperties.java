package br.edu.ifsp.kaluana.security;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** app.rate-limit.*: tentativas por janela nas rotas de autenticação e emparelhamento. */
@Validated
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
        @Positive long maxAttempts,
        @Positive long windowSeconds
) {
}
