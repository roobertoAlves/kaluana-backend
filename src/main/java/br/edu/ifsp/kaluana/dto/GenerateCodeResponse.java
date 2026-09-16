package br.edu.ifsp.kaluana.dto;

import java.time.OffsetDateTime;

public record GenerateCodeResponse(
        String code,
        OffsetDateTime expiresAt
) {
}
