package br.edu.ifsp.kaluana.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateCodeRequestDTO(
        @NotBlank(message = "Informe o código de sincronização.")
        String code
) {
}
