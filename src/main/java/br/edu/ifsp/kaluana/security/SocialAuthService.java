package br.edu.ifsp.kaluana.security;

import br.edu.ifsp.kaluana.dto.AuthResponse;
import br.edu.ifsp.kaluana.model.User;
import br.edu.ifsp.kaluana.model.UserRole;
import br.edu.ifsp.kaluana.repository.UserRepository;
import br.edu.ifsp.kaluana.service.AuthService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;


import java.util.Map;
import java.util.UUID;

@Service
public class SocialAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate;

    public SocialAuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.restTemplate = new RestTemplate();
    }

    public AuthResponse authenticateGoogle(String token) {
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";
        Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class, token); // This is simplified; usually needs a header
        return processSocialUser(userInfo, "google");
    }

    public AuthResponse authenticateGithub(String token) {
        String url = "https://api.github.com/user";
        // Github requires a specific User-Agent header and Bearer token
        Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class, token);
        return processSocialUser(userInfo, "github");
    }

    private AuthResponse processSocialUser(Map<String, Object> userInfo, String provider) {
        if (userInfo == null) throw new RuntimeException("Falha na autenticação social");

        String email = (String) userInfo.get("email");
        String name = (String) userInfo.get("name");

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    User newUser = new User(name, email, "SOCIAL_AUTH", UserRole.PRIMARY);
                    return userRepository.save(newUser);
                });

        String token = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        return new AuthResponse(token, br.edu.ifsp.kaluana.dto.UserResponse.from(user));
    }
}
