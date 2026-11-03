package br.edu.ifsp.kaluana.pairing;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A outra ponta de um emparelhamento, vista por quem está logado, e o que
 * o vínculo autoriza (o principal edita; o responsável só vê).
 */
public record LinkedUserResponseDTO(
        UUID userId,
        String name,
        String role,
        OffsetDateTime linkedAt,
        LinkPermissionsDTO permissions
) {
}
