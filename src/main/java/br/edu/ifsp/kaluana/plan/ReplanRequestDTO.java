package br.edu.ifsp.kaluana.plan;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

/** POST /api/day-plan/{date}/replan: o horário atual vem do aparelho, que conhece o fuso do usuário. */
public record ReplanRequestDTO(@NotNull @JsonFormat(pattern = "HH:mm") LocalTime now) {
}
