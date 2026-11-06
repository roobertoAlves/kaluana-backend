package br.edu.ifsp.kaluana.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        boolean requireConfirmation,
        @NotBlank String from,
        @NotBlank String confirmationBaseUrl,
        @Positive long tokenExpirationHours
) {
}
