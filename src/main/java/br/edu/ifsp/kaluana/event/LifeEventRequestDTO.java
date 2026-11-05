package br.edu.ifsp.kaluana.event;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LifeEventRequestDTO(

        @NotNull(message = "Escolha uma categoria.")
        EventCategory category,

        @Size(max = 500, message = "A nota pode ter até 500 caracteres.")
        String text,

        boolean urgent,

        OffsetDateTime timestamp
) {
}
