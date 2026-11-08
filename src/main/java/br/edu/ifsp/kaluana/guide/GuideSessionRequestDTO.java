package br.edu.ifsp.kaluana.guide;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;

public record GuideSessionRequestDTO(
        @NotBlank(message = "Informe o guia.")
        @Pattern(regexp = "^[a-z0-9-]{1,40}$", message = "Identificador de guia inválido.")
        String guideId,

        @Min(value = 1, message = "Versão do guia inválida.")
        int guideVersion,

        @NotNull(message = "Informe como a sessão terminou.")
        GuideSessionStatus status,

        @Min(value = 0, message = "Quantidade de etapas inválida.")
        @Max(value = 50, message = "Quantidade de etapas inválida.")
        int stepsCount,

        @NotNull(message = "Informe o início.")
        OffsetDateTime startedAt,

        @NotNull(message = "Informe o fim.")
        OffsetDateTime finishedAt
) {
}
