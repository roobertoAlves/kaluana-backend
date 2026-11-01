package br.edu.ifsp.kaluana.auth;
import br.edu.ifsp.kaluana.user.UserRole;
import br.edu.ifsp.kaluana.user.UserRepository;
import br.edu.ifsp.kaluana.pairing.PairingService;
import br.edu.ifsp.kaluana.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PairingService pairingService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Test
    void deveCriarContaPrimariaQuandoEmailDisponivel() {
        AuthService authService = new AuthService(userRepository, pairingService, passwordEncoder , jwtService);
        RegisterPrimaryRequestDTO request = new RegisterPrimaryRequestDTO("Maria", "maria@example.com", "senhaSegura1");

        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senhaSegura1")).thenReturn("hash-fake");
        when(jwtService.generateAccessToken(any(), anyString())).thenReturn("token-fake");

        AuthResponseDTO response = authService.registerPrimary(request);

        assertThat(response.accessToken()).isEqualTo("token-fake");
        assertThat(response.user().name()).isEqualTo("Maria");
        assertThat(response.user().role()).isEqualTo(UserRole.PRIMARY.name());
    }

    @Test
    void deveRecusarCadastroComEmailJaExistente() {
        AuthService authService = new AuthService(userRepository, pairingService, passwordEncoder , jwtService);
        RegisterPrimaryRequestDTO request = new RegisterPrimaryRequestDTO("Maria", "maria@example.com", "senhaSegura1");

        when(userRepository.existsByEmailIgnoreCase("maria@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerPrimary(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("já está cadastrado");
    }

    @Test
    void deveCompararSenhaMesmoQuandoEmailNaoExiste() {
        AuthService authService = new AuthService(userRepository, pairingService, passwordEncoder , jwtService);

        when(userRepository.findByEmailIgnoreCase("ninguem@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash-ficticio");

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("ninguem@example.com", "qualquer")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("E-mail ou senha inválidos");
        // O BCrypt roda nos dois casos: o tempo de resposta não revela se o e-mail existe.
        verify(passwordEncoder).matches("qualquer", "hash-ficticio");
    }
}
