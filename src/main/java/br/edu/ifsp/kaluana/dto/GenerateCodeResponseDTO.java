package br.edu.ifsp.kaluana.dto;

import java.time.OffsetDateTime;

public record GenerateCodeResponseDTO(
        String code,
        OffsetDateTime expiresAt
) {
}
