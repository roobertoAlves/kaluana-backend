package br.edu.ifsp.kaluana.service;

import br.edu.ifsp.kaluana.dto.AuthResponse;
import br.edu.ifsp.kaluana.dto.LoginRequest;
import br.edu.ifsp.kaluana.dto.RegisterPrimaryRequest;
import br.edu.ifsp.kaluana.dto.RegisterResponsibleRequest;
import br.edu.ifsp.kaluana.dto.UserResponse;
import br.edu.ifsp.kaluana.model.User;
import br.edu.ifsp.kaluana.model.UserRole;
import br.edu.ifsp.kaluana.repository.UserRepository;
import br.edu.ifsp.kaluana.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PairingService pairingService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PairingService pairingService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.pairingService = pairingService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registerPrimary(RegisterPrimaryRequest request) {
        garantirEmailDisponivel(request.email());

        User user = new User(
                request.name().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()),
                UserRole.PRIMARY);

        userRepository.save(user);
        return gerarResposta(user);
    }

    @Transactional
    public AuthResponse registerResponsible(RegisterResponsibleRequest request) {
        garantirEmailDisponivel(request.email());

        // Revalida e consome o código dentro desta mesma transação: se algo
        // falhar depois, tudo desfaz junto (rollback), incluindo o consumo.
        UUID primaryUserId = pairingService.consumeCode(request.code());

        User user = new User(
                request.name().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()),
                UserRole.RESPONSIBLE);

        userRepository.save(user);
        pairingService.createLink(user.getId(), primaryUserId);

        return gerarResposta(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(this::credenciaisInvalidas);

        if (!passwordEncoder.matches(request.password(), user.getHashPassword())) {
            throw credenciaisInvalidas();
        }

        return gerarResposta(user);
    }

    private void garantirEmailDisponivel(String email) {
        if (userRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }
    }

    private ResponseStatusException credenciaisInvalidas() {
        // Mensagem genérica de propósito: não revela se o e-mail existe ou não.
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
    }

    private AuthResponse gerarResposta(User user) {
        String token = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        return new AuthResponse(token, UserResponse.from(user));
    }
}
