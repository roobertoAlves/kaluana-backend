package br.edu.ifsp.kaluana.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * app.mail.*: confirmação de e-mail no cadastro. O servidor SMTP em si fica
 * em spring.mail.* (Mailpit no desenvolvimento; Gmail ou outro depois, só
 * trocando variáveis de ambiente).
 *
 * @param requireConfirmation se false, a conta já nasce confirmada (testes).
 * @param confirmationBaseUrl endereço público da API usado no link do e-mail.
 */
@Validated
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        boolean requireConfirmation,
        @NotBlank String from,
        @NotBlank String confirmationBaseUrl,
        @Positive long tokenExpirationHours
) {
}
