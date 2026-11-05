package br.edu.ifsp.kaluana.report;

import br.edu.ifsp.kaluana.access.AccessSettings;
import br.edu.ifsp.kaluana.pairing.LinkPermissionsDTO;
import br.edu.ifsp.kaluana.pairing.LinkRelationship;
import br.edu.ifsp.kaluana.pairing.ResponsibleLink;
import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRole;
import br.edu.ifsp.kaluana.checkin.CheckInRepository;
import br.edu.ifsp.kaluana.task.TaskRepository;
import br.edu.ifsp.kaluana.event.LifeEventRepository;
import br.edu.ifsp.kaluana.timer.TimerSessionRepository;
import br.edu.ifsp.kaluana.user.UserRepository;
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
    private PairingService pairingService;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                userRepository,
                lifeEventRepository,
                checkInRepository,
                timerSessionRepository,
                taskRepository,
                ambientSessionRepository,
                pairingService);
    }

    @Test
    void deveRecusarRelatorioQuandoNaoHaVinculo() {
        when(pairingService.findLink(RESPONSIBLE_ID, PRIMARY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.linkedReport(RESPONSIBLE_ID, PRIMARY_ID, 7))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deveRecusarRelatorioQuandoUsuarioNaoLiberouParaEssaPessoa() {
        when(pairingService.findLink(RESPONSIBLE_ID, PRIMARY_ID))
                .thenReturn(Optional.of(link(new LinkPermissionsDTO(LinkRelationship.FAMILY, true, true, true, true, false))));

        assertThatThrownBy(() -> reportService.linkedReport(RESPONSIBLE_ID, PRIMARY_ID, 7))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void deveIncluirSoAsSecoesAutorizadasParaAquelaPessoa() {
        when(pairingService.findLink(RESPONSIBLE_ID, PRIMARY_ID))
                .thenReturn(Optional.of(link(new LinkPermissionsDTO(LinkRelationship.HEALTH_PROFESSIONAL, false, true, false, true, true))));
        when(userRepository.findById(PRIMARY_ID)).thenReturn(Optional.of(user("Maria", UserRole.PRIMARY)));
        when(userRepository.findById(RESPONSIBLE_ID)).thenReturn(Optional.of(user("Dra. Ana", UserRole.RESPONSIBLE)));
        when(checkInRepository.findByUserIdAndDayGreaterThanEqualOrderByDayAsc(any(), any())).thenReturn(List.of());
        when(taskRepository.findTop300ByUserIdAndStatusAndCompletedAtGreaterThanEqualOrderByCompletedAtAsc(any(), any(), any()))
                .thenReturn(List.of());

        ReportResponseDTO report = reportService.linkedReport(RESPONSIBLE_ID, PRIMARY_ID, 7);

        assertThat(report.shared()).isTrue();
        assertThat(report.preparedFor()).isEqualTo("Dra. Ana");
        assertThat(report.relationship()).isEqualTo(LinkRelationship.HEALTH_PROFESSIONAL);
        assertThat(report.checkIns()).isEmpty();
        assertThat(report.tasks()).isNotNull();
        assertThat(report.events()).isNull();
        assertThat(report.usage()).isNull();
        verify(lifeEventRepository, never()).countByCategorySince(any(), any());
    }

    @Test
    void novoVinculoComecaComOPadraoDoUsuario() {
        AccessSettings padrao = AccessSettings.nothingSharedFor(PRIMARY_ID);
        padrao.update(true, false, false, true);

        ResponsibleLink link = ResponsibleLink.withDefaults(RESPONSIBLE_ID, PRIMARY_ID, padrao);

        assertThat(link.isShareEvents()).isTrue();
        assertThat(link.isShareCheckIns()).isFalse();
        assertThat(link.isShareReports()).isTrue();
        assertThat(link.isShareTasks()).isFalse();
        assertThat(link.getRelationship()).isEqualTo(LinkRelationship.OTHER);
    }

    @Test
    void deveRecusarPeriodoForaDoLimite() {
        assertThatThrownBy(() -> reportService.ownReport(PRIMARY_ID, 0))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("1 a 365");
        verify(userRepository, never()).findById(eq(PRIMARY_ID));
    }

    private static ResponsibleLink link(LinkPermissionsDTO permissions) {
        ResponsibleLink link = new ResponsibleLink(RESPONSIBLE_ID, PRIMARY_ID);
        link.updatePermissions(permissions);
        return link;
    }

    private static User user(String name, UserRole role) {
        return new User(name, name.toLowerCase().replace(" ", "") + "@example.com", "hash", role);
    }
}
