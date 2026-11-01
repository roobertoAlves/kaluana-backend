package br.edu.ifsp.kaluana.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LifeEventResponseDTO(
        UUID id,
        EventCategory category,
        String text,
        boolean urgent,
        OffsetDateTime timestamp
) {
    public static LifeEventResponseDTO from(LifeEvent event) {
        return new LifeEventResponseDTO(
                event.getId(), event.getCategory(), event.getNote(), event.isUrgent(), event.getOccurredAt());
    }
}
