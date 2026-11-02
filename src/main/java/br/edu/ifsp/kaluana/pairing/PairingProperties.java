package br.edu.ifsp.kaluana.pairing;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** app.pairing.*: validade do código de emparelhamento (RF6). */
@Validated
@ConfigurationProperties(prefix = "app.pairing")
public record PairingProperties(@Positive long codeExpirationMinutes) {
}
