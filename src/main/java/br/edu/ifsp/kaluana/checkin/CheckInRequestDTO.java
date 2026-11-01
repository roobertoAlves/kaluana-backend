package br.edu.ifsp.kaluana.checkin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CheckInRequestDTO(

        @Min(value = 1, message = "Escolha um nível de 1 a 5.")
        @Max(value = 5, message = "Escolha um nível de 1 a 5.")
        int level
) {
}
