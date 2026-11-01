package br.edu.ifsp.kaluana.pairing;

import java.time.OffsetDateTime;
import java.util.UUID;

/** A outra ponta de um emparelhamento, vista por quem está logado. */
public record LinkedUserResponseDTO(
        UUID userId,
        String name,
        String role,
        OffsetDateTime linkedAt
) {
}
