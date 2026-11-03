package br.edu.ifsp.kaluana.reminder;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReminderRequestDTO(

        @NotBlank(message = "Dê um nome ao lembrete.")
        @Size(max = 80, message = "O nome pode ter até 80 caracteres.")
        String title,

        @NotNull(message = "Informe o horário.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,

        @Min(value = 0, message = "A antecedência não pode ser negativa.")
        @Max(value = 1440, message = "A antecedência pode ser de até 24 horas.")
        int leadMinutes,

        @NotNull(message = "Escolha a frequência.")
        ReminderFrequency frequency,

        @NotNull(message = "Escolha a insistência.")
        ReminderInsistence insistence,

        Boolean enabled
) {
    public boolean enabledOrDefault() {
        return enabled == null || enabled;
    }
}
