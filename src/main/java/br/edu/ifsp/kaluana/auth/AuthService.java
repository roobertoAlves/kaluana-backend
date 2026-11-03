package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.pairing.PairingService;

import br.edu.ifsp.kaluana.user.UserResponseDTO;
import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRole;
import br.edu.ifsp.kaluana.user.UserRepository;
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
    private final RefreshTokenService refreshTokenService;
    private volatile String hashFicticio;

    public AuthService(
            UserRepository userRepository,
            PairingService pairingService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.pairingService = pairingService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponseDTO registerPrimary(RegisterPrimaryRequestDTO request) {
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
    public AuthResponseDTO registerResponsible(RegisterResponsibleRequestDTO request) {
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

    // Não é readOnly: o login grava o token de renovação.
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim()).orElse(null);

        // Sem usuário, compara com um hash qualquer mesmo assim: a resposta leva
        // o mesmo tempo nos dois casos e não revela quais e-mails têm conta.
        String hash = user != null ? user.getHashPassword() : hashFicticio();
        if (!passwordEncoder.matches(request.password(), hash) || user == null) {
            throw credenciaisInvalidas();
        }

        return gerarResposta(user);
    }

    /** RNF4: troca o token de renovação por um par novo (acesso + renovação). */
    @Transactional
    public AuthResponseDTO refresh(RefreshRequestDTO request) {
        RefreshTokenService.Rotated rotated = refreshTokenService.rotate(request.refreshToken());
        User user = userRepository.findById(rotated.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, RefreshTokenService.SESSION_EXPIRED));
        return gerarResposta(user, rotated.refreshToken());
    }

    @Transactional
    public void logout(RefreshRequestDTO request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private String hashFicticio() {
        if (hashFicticio == null) {
            hashFicticio = passwordEncoder.encode("kaluana-usuario-inexistente");
        }
        return hashFicticio;
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

    private AuthResponseDTO gerarResposta(User user) {
        return gerarResposta(user, refreshTokenService.issue(user.getId()));
    }

    private AuthResponseDTO gerarResposta(User user, String refreshToken) {
        String token = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        return new AuthResponseDTO(token, refreshToken, UserResponseDTO.from(user));
    }
}
