package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.user.UserResponseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Sessão devolvida no login e no cadastro. Cadastro que ainda precisa
 * confirmar o e-mail vem sem tokens (só o usuário, com emailVerified=false).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDTO(String accessToken, String refreshToken, UserResponseDTO user) {}
