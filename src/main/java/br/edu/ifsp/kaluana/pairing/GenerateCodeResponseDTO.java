package br.edu.ifsp.kaluana.pairing;

import java.time.OffsetDateTime;

public record GenerateCodeResponseDTO(
        String code,
        OffsetDateTime expiresAt
) {
}
