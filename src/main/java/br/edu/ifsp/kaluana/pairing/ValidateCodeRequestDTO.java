package br.edu.ifsp.kaluana.pairing;

import jakarta.validation.constraints.NotBlank;

public record ValidateCodeRequestDTO(
        @NotBlank(message = "Informe o código de sincronização.")
        String code
) {
}
