package br.edu.ifsp.kaluana.auth.dto;

import br.edu.ifsp.kaluana.user.User;

import java.util.UUID;

public record UserResponse(

    UUID id,
    String name,
    String email,
    String role
){
    public static UserResponse from(User user){
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
