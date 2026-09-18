package br.edu.ifsp.kaluana.dto;

import br.edu.ifsp.kaluana.model.User;

import java.util.UUID;

public record UserResponseDTO(

    UUID id,
    String name,
    String email,
    String role
){
    public static UserResponseDTO from(User user){
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
