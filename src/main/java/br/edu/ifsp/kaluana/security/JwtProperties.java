package br.edu.ifsp.kaluana.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * app.jwt.*: o segredo vem só de variável de ambiente (JWT_SECRET). Validado
 * na subida: HS256 exige pelo menos 32 bytes, e sem segredo o app não sobe.
 * O token de acesso dura minutos; o de renovação, dias (RNF4).
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "JWT_SECRET precisa ter pelo menos 32 caracteres.") String secret,
        @Positive long accessTokenExpirationMinutes,
        @Positive long refreshTokenExpirationDays
) {
}
