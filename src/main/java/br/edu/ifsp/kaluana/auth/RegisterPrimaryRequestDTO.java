package br.edu.ifsp.kaluana.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public  record RegisterPrimaryRequestDTO(

        @NotBlank(message = "Informe o nome.")
        String name,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.")
        String email,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 8, message = "A senha precisa ter pelo menos 8 caracteres.")
        String password
){
}
