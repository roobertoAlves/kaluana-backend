package br.edu.ifsp.kaluana.timer;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimerSessionRequestDTO(

        // Opcional: tarefa (RF5) que originou a sessão.
        UUID taskId,

        @NotBlank(message = "Informe a tarefa.")
        @Size(max = 80, message = "A tarefa pode ter até 80 caracteres.")
        String task,

        @Min(value = 1, message = "A sessão precisa ter pelo menos 1 minuto.")
        @Max(value = 600, message = "A sessão pode ter até 600 minutos.")
        int plannedMinutes,

        @Min(value = 0, message = "Quantidade de etapas inválida.")
        @Max(value = 4, message = "A sessão pode ter até 4 etapas.")
        int stepsCount,

        boolean water,

        // Ausente = sem som.
        TimerSound sound,

        @NotNull(message = "Informe como a sessão terminou.")
        TimerSessionStatus status,

        // Horários vêm do relógio do aparelho; não validamos "no futuro" para
        // não rejeitar sessões de quem está com o relógio um pouco adiantado.
        @NotNull(message = "Informe o início da sessão.")
        OffsetDateTime startedAt,

        @NotNull(message = "Informe o fim da sessão.")
        OffsetDateTime finishedAt
) {
    public TimerSound soundOrDefault() {
        return sound != null ? sound : TimerSound.NONE;
    }

    @AssertTrue(message = "O fim da sessão precisa ser depois do início.")
    public boolean isPeriodValid() {
        return startedAt == null || finishedAt == null || !finishedAt.isBefore(startedAt);
    }
}
