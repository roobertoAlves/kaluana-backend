package br.edu.ifsp.kaluana.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskStepRequestDTO(

        @NotBlank(message = "Descreva a microetapa.")
        @Size(max = 120, message = "A microetapa pode ter até 120 caracteres.")
        String text,

        boolean done
) {
}
