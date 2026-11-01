package br.edu.ifsp.kaluana.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrustedContactRequestDTO(

        @NotBlank(message = "Informe um nome.")
        @Size(max = 80, message = "O nome pode ter até 80 caracteres.")
        String name,

        @NotNull(message = "Escolha a forma de contato.")
        ContactMethod method,

        @NotBlank(message = "Informe o telefone ou e-mail.")
        @Size(max = 255, message = "O contato pode ter até 255 caracteres.")
        String value
) {
}
