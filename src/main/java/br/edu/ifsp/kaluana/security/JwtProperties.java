package br.edu.ifsp.kaluana.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "JWT_SECRET precisa ter pelo menos 32 caracteres.") String secret,
        @Positive long accessTokenExpirationMinutes,
        @Positive long refreshTokenExpirationDays
) {
}
