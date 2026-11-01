package br.edu.ifsp.kaluana.event;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record LifeEventRequestDTO(

        @NotNull(message = "Escolha uma categoria.")
        EventCategory category,

        @Size(max = 500, message = "A nota pode ter até 500 caracteres.")
        String text,

        boolean urgent,

        // Opcional: se vier vazio (ou no futuro), vale o horário do servidor.
        OffsetDateTime timestamp
) {
}
