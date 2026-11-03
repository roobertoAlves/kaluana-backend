package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.security.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");
    private static final UUID USUARIO = UUID.randomUUID();

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties("x".repeat(32), 30, 30);
        service = new RefreshTokenService(repository, props, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void deveGuardarSoOHashDoToken() {
        String raw = service.issue(USUARIO);

        ArgumentCaptor<RefreshToken> salvo = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(salvo.capture());
        assertThat(raw).hasSizeGreaterThan(40);
        assertThat(salvo.getValue().getUserId()).isEqualTo(USUARIO);
        assertThat(salvo.getValue().isUsable(OffsetDateTime.now(Clock.fixed(AGORA, ZoneOffset.UTC)))).isTrue();
    }

    @Test
    void deveTrocarOTokenAoRenovar() {
        RefreshToken atual = token(AGORA.plusSeconds(3600));
        when(repository.findByTokenHash(RefreshTokenService.hash("antigo"))).thenReturn(Optional.of(atual));

        RefreshTokenService.Rotated rotated = service.rotate("antigo");

        assertThat(rotated.userId()).isEqualTo(USUARIO);
        assertThat(rotated.refreshToken()).isNotEqualTo("antigo");
        assertThat(atual.isRevoked()).isTrue();
    }

    @Test
    void deveRecusarTokenVencido() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(token(AGORA.minusSeconds(1))));

        assertThatThrownBy(() -> service.rotate("vencido"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Sua sessão expirou");
        verify(repository, never()).save(any());
    }

    @Test
    void deveEncerrarTodasAsSessoesQuandoTokenJaUsadoReaparece() {
        RefreshToken usado = token(AGORA.plusSeconds(3600));
        usado.revoke(OffsetDateTime.ofInstant(AGORA, ZoneOffset.UTC));
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(usado));

        assertThatThrownBy(() -> service.rotate("copiado")).isInstanceOf(ResponseStatusException.class);
        verify(repository).revokeAllOf(eq(USUARIO), any());
    }

    private static RefreshToken token(Instant expira) {
        return new RefreshToken(USUARIO, "hash", OffsetDateTime.ofInstant(expira, ZoneOffset.UTC));
    }
}
