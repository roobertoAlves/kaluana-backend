package br.edu.ifsp.kaluana.ambient;

import br.edu.ifsp.kaluana.timer.TimerSessionStatus;
import br.edu.ifsp.kaluana.timer.TimerSound;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record AmbientSessionRequestDTO(

        @NotNull(message = "Informe o modo (RELAX ou SLEEP).")
        AmbientMode mode,

        @NotNull(message = "Informe o som.")
        TimerSound sound,

        @Min(value = 1, message = "A sessão precisa ter pelo menos 1 minuto.")
        @Max(value = 120, message = "A sessão pode ter até 120 minutos.")
        int plannedMinutes,

        @NotNull(message = "Informe como a sessão terminou.")
        TimerSessionStatus status,

        @NotNull(message = "Informe o início da sessão.")
        OffsetDateTime startedAt,

        @NotNull(message = "Informe o fim da sessão.")
        OffsetDateTime finishedAt
) {
    @AssertTrue(message = "Relaxar e Dormir precisam de um som.")
    public boolean isSoundChosen() {
        return sound != TimerSound.NONE;
    }

    @AssertTrue(message = "O fim da sessão precisa ser depois do início.")
    public boolean isPeriodValid() {
        return startedAt == null || finishedAt == null || !finishedAt.isBefore(startedAt);
    }
}
