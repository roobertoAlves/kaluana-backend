package br.edu.ifsp.kaluana.plan;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;

public record ReplanRequestDTO(@NotNull @JsonFormat(pattern = "HH:mm") LocalTime now) {
}
