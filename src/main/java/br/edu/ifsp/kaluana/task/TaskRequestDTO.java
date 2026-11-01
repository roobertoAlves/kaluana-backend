package br.edu.ifsp.kaluana.task;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Corpo de criação e de edição. {@code steps} só é usado na criação (atalho
 * para já criar a tarefa com microetapas); na edição, as etapas têm rotas próprias.
 */
public record TaskRequestDTO(

        @NotBlank(message = "Dê um nome à tarefa.")
        @Size(max = 120, message = "O nome pode ter até 120 caracteres.")
        String title,

        @NotNull(message = "Escolha a prioridade.")
        TaskPriority priority,

        @Min(value = 5, message = "A duração mínima é de 5 minutos.")
        @Max(value = 480, message = "A duração máxima é de 8 horas.")
        Integer estimatedMinutes,

        LocalDate dueDate,

        Boolean done,

        @Size(max = Task.MAX_STEPS, message = "Uma tarefa pode ter até 12 microetapas.")
        List<@NotBlank(message = "Microetapa vazia.") @Size(max = 120, message = "Cada microetapa pode ter até 120 caracteres.") String> steps
) {
}
