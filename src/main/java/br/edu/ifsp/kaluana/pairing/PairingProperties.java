package br.edu.ifsp.kaluana.pairing;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "app.pairing")
public record PairingProperties(@Positive long codeExpirationMinutes) {
}
