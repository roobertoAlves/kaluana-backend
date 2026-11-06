package br.edu.ifsp.kaluana.user;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO me(UUID userId) {
        return UserResponseDTO.from(findUser(userId));
    }

    @Transactional
    public UserResponseDTO updateProfile(UUID userId, UpdateProfileRequestDTO request) {
        User user = findUser(userId);
        user.updateProfile(request.name().trim(), blankToNull(request.nickname()), request.avatarColor());
        return UserResponseDTO.from(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
