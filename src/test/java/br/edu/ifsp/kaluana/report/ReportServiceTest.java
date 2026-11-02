package br.edu.ifsp.kaluana.report;

import br.edu.ifsp.kaluana.access.AccessSettings;
import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRole;
import br.edu.ifsp.kaluana.checkin.CheckInRepository;
import br.edu.ifsp.kaluana.task.TaskRepository;
import br.edu.ifsp.kaluana.event.LifeEventRepository;
import br.edu.ifsp.kaluana.timer.TimerSessionRepository;
import br.edu.ifsp.kaluana.user.UserRepository;
import br.edu.ifsp.kaluana.access.AccessSettingsService;
import br.edu.ifsp.kaluana.ambient.AmbientSessionRepository;
import br.edu.ifsp.kaluana.pairing.PairingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    private static final UUID PRIMARY_ID = UUID.randomUUID();
    private static final UUID RESPONSIBLE_ID = UUID.randomUUID();

    @Mock
    private UserRepository userRepository;

    @Mock
    private LifeEventRepository lifeEventRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private TimerSessionRepository timerSessionRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AmbientSessionRepository ambientSessionRepository;

    @Mock
    private AccessSettingsService accessSettingsService;

    @Mock
    private PairingService pairingService;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(userRepository, lifeEventRepository, checkInRepository,
                timerSessionRepository, taskRepository, ambientSessionRepository, accessSettingsService, pairingService);
    }

    @Test
    void deveRecusarRelatorioQuandoNaoHaVinculo() {
        when(pairingService.isLinked(RESPONSIBLE_ID, PRIMARY_ID)).thenReturn(false);

        assertThatThrownBy(() -> reportService.linkedReport(RESPONSIBLE_ID, PRIMARY_ID, 7))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deveRecusarRelatorioQuandoUsuarioNaoLiberou() {
        when(pairingService.isLinked(RESPONSIBLE_ID, PRIMARY_ID)).thenReturn(true);
        when(accessSettingsService.find(PRIMARY_ID)).thenReturn(AccessSettings.nothingSharedFor(PRIMARY_ID));

        assertThatThrownBy(() -> reportService.linkedReport(RESPONSIBLE_ID, PRIMARY_ID, 7))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void deveIncluirSoAsSecoesAutorizadas() {
        AccessSettings access = AccessSettings.nothingSharedFor(PRIMARY_ID);
        access.update(false, true, false, true);

        when(pairingService.isLinked(RESPONSIBLE_ID, PRIMARY_ID)).thenReturn(true);
        when(accessSettingsService.find(PRIMARY_ID)).thenReturn(access);
        when(userRepository.findById(PRIMARY_ID)).thenReturn(Optional.of(primaryUser()));
        when(checkInRepository.findByUserIdAndDayGreaterThanEqualOrderByDayAsc(eq(PRIMARY_ID), any())).thenReturn(List.of());

        ReportResponseDTO report = reportService.linkedReport(RESPONSIBLE_ID, PRIMARY_ID, 7);

        assertThat(report.shared()).isTrue();
        assertThat(report.checkIns()).isEmpty();
        assertThat(report.events()).isNull();
        assertThat(report.usage()).isNull();
        verify(lifeEventRepository, never()).countByCategorySince(any(), any());
    }

    @Test
    void deveRecusarPeriodoForaDoLimite() {
        assertThatThrownBy(() -> reportService.ownReport(PRIMARY_ID, 0))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("1 a 365");
    }

    private static User primaryUser() {
        return new User("Maria", "maria@example.com", "hash", UserRole.PRIMARY);
    }
}
