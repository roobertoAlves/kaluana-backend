package br.edu.ifsp.kaluana.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDTO(

        @NotBlank(message = "Informe o nome.")
        @Size(max = 150, message = "O nome pode ter até 150 caracteres.")
        String name,

        @Size(max = 30, message = "O apelido pode ter até 30 caracteres.")
        String nickname,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor do avatar inválida.")
        String avatarColor
) {
}
