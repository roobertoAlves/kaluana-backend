package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.user.UserResponseDTO;

public record AuthResponseDTO(String accessToken, UserResponseDTO user) {}
