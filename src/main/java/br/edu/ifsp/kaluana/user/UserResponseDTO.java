package br.edu.ifsp.kaluana.user;

import java.util.UUID;

public record UserResponseDTO(

    UUID id,
    String name,
    String email,
    String role,
    String nickname,
    String avatarColor
){
    public static UserResponseDTO from(User user){
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getNickname(),
                user.getAvatarColor());
    }
}
