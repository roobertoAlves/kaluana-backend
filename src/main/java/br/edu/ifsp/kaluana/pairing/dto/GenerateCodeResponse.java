package br.edu.ifsp.kaluana.pairing.dto;

import java.time.OffsetDateTime;

public record GenerateCodeResponse(
        String code,
        OffsetDateTime expiresAt
) {
}
