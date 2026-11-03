package br.edu.ifsp.kaluana.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequestDTO(

    @NotBlank(message = "Informe o token de renovação.")
    @Size(max = 200)
    String refreshToken
){
}
