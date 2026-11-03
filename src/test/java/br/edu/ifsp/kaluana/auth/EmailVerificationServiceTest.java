package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.auth.EmailVerificationService.EmailConfirmationRequested;
import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRepository;
import br.edu.ifsp.kaluana.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-03T12:00:00Z");

    @Mock
    private EmailVerificationTokenRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher events;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        MailProperties props = new MailProperties(true, "Kaluanã <nao-responda@kaluana.local>", "http://localhost:8080/", 24);
        service = new EmailVerificationService(repository, userRepository, events, props, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void enviaLinkComTokenQueSoTemOHashNoBanco() {
        service.sendConfirmation(new User("Maria", "maria@example.com", "hash", UserRole.PRIMARY));

        ArgumentCaptor<EmailConfirmationRequested> pedido = ArgumentCaptor.forClass(EmailConfirmationRequested.class);
        verify(events).publishEvent(pedido.capture());
        String link = pedido.getValue().link();
        assertThat(link).startsWith("http://localhost:8080/api/auth/confirm-email?token=");
        String raw = link.substring(link.indexOf('=') + 1);

        ArgumentCaptor<EmailVerificationToken> salvo = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(repository).save(salvo.capture());
        assertThat(salvo.getValue().isUsable(OffsetDateTime.ofInstant(AGORA, ZoneOffset.UTC).plusHours(23))).isTrue();
        assertThat(salvo.getValue().isUsable(OffsetDateTime.ofInstant(AGORA, ZoneOffset.UTC).plusHours(25))).isFalse();
        verify(repository).save(any());
        assertThat(raw).isNotBlank();
    }

    @Test
    void cliqueNoLinkAtivaAConta() {
        User maria = new User("Maria", "maria@example.com", "hash", UserRole.PRIMARY);
        EmailVerificationToken token = new EmailVerificationToken(null, "h", OffsetDateTime.ofInstant(AGORA, ZoneOffset.UTC).plusHours(1));
        when(repository.findByTokenHash(RefreshTokenService.hash("abc"))).thenReturn(Optional.of(token));
        when(userRepository.findById(any())).thenReturn(Optional.of(maria));

        service.confirm("abc");

        assertThat(maria.isEmailVerified()).isTrue();
        assertThatThrownBy(() -> service.confirm("abc")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void linkVencidoNaoAtiva() {
        EmailVerificationToken token = new EmailVerificationToken(null, "h", OffsetDateTime.ofInstant(AGORA, ZoneOffset.UTC).minusMinutes(1));
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.confirm("velho"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não vale mais");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void reenvioIgnoraContaJaConfirmadaOuInexistente() {
        User confirmada = new User("Ana", "ana@example.com", "hash", UserRole.PRIMARY);
        confirmada.markEmailVerified();
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(confirmada));
        when(userRepository.findByEmailIgnoreCase("ninguem@example.com")).thenReturn(Optional.empty());

        service.resend("ana@example.com");
        service.resend("ninguem@example.com");

        verify(events, never()).publishEvent(any());
    }
}
