package br.edu.ifsp.kaluana.dto;

import  jakarta.validation.constraints.NotBlank;

public record LoginRequest(

    @NotBlank(message = "Informe o e-mail.")
    String email,

    @NotBlank(message = "Informe a senha.")
    String password
){
    public static record ValidateCodeResponse(
            boolean valid,
            String mainPersonName
    ) {
    }
}
