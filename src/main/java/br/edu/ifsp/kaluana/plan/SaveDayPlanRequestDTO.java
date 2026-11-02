package br.edu.ifsp.kaluana.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/** PUT /api/day-plan/{date}: monta e salva o plano a partir de {@code start} (horário do aparelho). */
public record SaveDayPlanRequestDTO(
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime start,
        @NotNull @JsonFormat(pattern = "HH:mm") LocalTime end,
        boolean assistant
) {
}
