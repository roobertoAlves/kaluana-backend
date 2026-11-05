package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.user.UserResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDTO(String accessToken, String refreshToken, UserResponseDTO user) {}
