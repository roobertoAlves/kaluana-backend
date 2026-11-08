package br.edu.ifsp.kaluana.guide;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuideSessionServiceTest {

    private static final OffsetDateTime INICIO = OffsetDateTime.parse("2026-11-08T10:00:00-03:00");

    @Mock
    private GuideSessionRepository repository;

    @Test
    void registraSoOResumoDaSessao() {
        GuideSessionService service = new GuideSessionService(repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GuideSessionResponseDTO salvo = service.record(UUID.randomUUID(),
                new GuideSessionRequestDTO("sobrecarga", 1, GuideSessionStatus.COMPLETED, 4, INICIO, INICIO.plusMinutes(3)));

        assertThat(salvo.status()).isEqualTo(GuideSessionStatus.COMPLETED);
        assertThat(salvo.stepsCount()).isEqualTo(4);
    }

    @Test
    void recusaFimAntesDoInicio() {
        GuideSessionService service = new GuideSessionService(repository);

        assertThatThrownBy(() -> service.record(UUID.randomUUID(),
                new GuideSessionRequestDTO("sobrecarga", 1, GuideSessionStatus.ABANDONED, 1, INICIO, INICIO.minusMinutes(1))))
                .isInstanceOf(ResponseStatusException.class);
        verify(repository, never()).save(any());
    }
}
