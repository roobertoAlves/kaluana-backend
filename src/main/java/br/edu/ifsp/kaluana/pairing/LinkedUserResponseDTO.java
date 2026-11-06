package br.edu.ifsp.kaluana.pairing;

import java.time.OffsetDateTime;
import java.util.UUID;


public record LinkedUserResponseDTO(
        UUID userId,
        String name,
        String role,
        OffsetDateTime linkedAt,
        LinkPermissionsDTO permissions
) {
}
