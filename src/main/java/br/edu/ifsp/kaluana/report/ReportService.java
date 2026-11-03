package br.edu.ifsp.kaluana.report;

import br.edu.ifsp.kaluana.pairing.PairingService;
import br.edu.ifsp.kaluana.pairing.ResponsibleLink;

import br.edu.ifsp.kaluana.report.ReportResponseDTO.CheckInEntry;
import br.edu.ifsp.kaluana.report.ReportResponseDTO.EventEntry;
import br.edu.ifsp.kaluana.report.ReportResponseDTO.EventsSection;
import br.edu.ifsp.kaluana.report.ReportResponseDTO.TaskEntry;
import br.edu.ifsp.kaluana.report.ReportResponseDTO.TasksSection;
import br.edu.ifsp.kaluana.report.ReportResponseDTO.UsageSection;
import br.edu.ifsp.kaluana.event.EventCategory;
import br.edu.ifsp.kaluana.task.TaskRepository;
import br.edu.ifsp.kaluana.task.TaskStatus;
import br.edu.ifsp.kaluana.timer.TimerSessionStatus;
import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.checkin.CheckInRepository;
import br.edu.ifsp.kaluana.event.LifeEventRepository;
import br.edu.ifsp.kaluana.timer.TimerSessionRepository;
import br.edu.ifsp.kaluana.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RF7: relatório informativo. Só conta e lista o que foi registrado; não
 * calcula pontuação, não classifica e não sugere nada (RNF6, diretriz NICE).
 * Para quem está vinculado, cada seção só entra se o usuário autorizou para
 * aquela pessoa (RF8/RNF3); é essa versão que o especialista exporta em PDF.
 */
@Service
public class ReportService {

    public static final String DISCLAIMER =
            "Relatório informativo gerado a partir de registros feitos pelo próprio usuário. "
                    + "Não é diagnóstico, não indica prognóstico nem recomendação de tratamento.";

    private static final int MAX_PERIOD_DAYS = 365;

    private final UserRepository userRepository;
    private final LifeEventRepository lifeEventRepository;
    private final CheckInRepository checkInRepository;
    private final TimerSessionRepository timerSessionRepository;
    private final TaskRepository taskRepository;
    private final PairingService pairingService;

    public ReportService(
            UserRepository userRepository,
            LifeEventRepository lifeEventRepository,
            CheckInRepository checkInRepository,
            TimerSessionRepository timerSessionRepository,
            TaskRepository taskRepository,
            PairingService pairingService) {
        this.userRepository = userRepository;
        this.lifeEventRepository = lifeEventRepository;
        this.checkInRepository = checkInRepository;
        this.timerSessionRepository = timerSessionRepository;
        this.taskRepository = taskRepository;
        this.pairingService = pairingService;
    }

    /** Relatório da própria pessoa: todas as seções. */
    @Transactional(readOnly = true)
    public ReportResponseDTO ownReport(UUID userId, int days) {
        validatePeriod(days);
        return build(findUser(userId), days, Scope.EVERYTHING, null, null);
    }

    /** Relatório de quem está emparelhado com o responsável, filtrado pelo que o vínculo autoriza. */
    @Transactional(readOnly = true)
    public ReportResponseDTO linkedReport(UUID responsibleId, UUID primaryUserId, int days) {
        validatePeriod(days);
        // 404 e não 403: não confirma para terceiros que a pessoa existe.
        ResponsibleLink link = pairingService.findLink(responsibleId, primaryUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vínculo não encontrado."));
        if (!link.isShareReports()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A pessoa ainda não liberou relatórios para você.");
        }

        User viewer = findUser(responsibleId);
        return build(findUser(primaryUserId), days, Scope.from(link), viewer.getName(), link);
    }

    private ReportResponseDTO build(User user, int days, Scope scope, String preparedFor, ResponsibleLink link) {
        UUID userId = user.getId();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime since = now.minusDays(days);

        return new ReportResponseDTO(
                user.getName(),
                days,
                now,
                link != null,
                preparedFor,
                link != null ? link.getRelationship() : null,
                DISCLAIMER,
                scope.events() ? eventsSection(userId, since) : null,
                scope.checkIns() ? checkInEntries(userId, since.toLocalDate()) : null,
                scope.usage() ? usageSection(userId, since) : null,
                scope.tasks() ? tasksSection(userId, since) : null);
    }

    private EventsSection eventsSection(UUID userId, OffsetDateTime since) {
        Map<EventCategory, Long> byCategory = new EnumMap<>(EventCategory.class);
        lifeEventRepository.countByCategorySince(userId, since)
                .forEach(count -> byCategory.put(count.getCategory(), count.getTotal()));

        long total = byCategory.values().stream().mapToLong(Long::longValue).sum();
        long urgent = lifeEventRepository.countByUserIdAndUrgentTrueAndOccurredAtGreaterThanEqual(userId, since);
        List<EventEntry> entries = lifeEventRepository
                .findTop500ByUserIdAndOccurredAtGreaterThanEqualOrderByOccurredAtAsc(userId, since).stream()
                .map(e -> new EventEntry(e.getOccurredAt(), e.getCategory(), e.isUrgent(), e.getNote()))
                .toList();
        return new EventsSection(total, byCategory, urgent, entries);
    }

    private List<CheckInEntry> checkInEntries(UUID userId, LocalDate since) {
        return checkInRepository.findByUserIdAndDayGreaterThanEqualOrderByDayAsc(userId, since).stream()
                .map(checkIn -> new CheckInEntry(checkIn.getDay(), checkIn.getLevel()))
                .toList();
    }

    private UsageSection usageSection(UUID userId, OffsetDateTime since) {
        return new UsageSection(
                timerSessionRepository.countByUserIdAndStatusAndFinishedAtGreaterThanEqual(userId, TimerSessionStatus.DONE, since),
                timerSessionRepository.countByUserIdAndStatusAndFinishedAtGreaterThanEqual(userId, TimerSessionStatus.CANCELED, since),
                taskRepository.countByUserIdAndStatusAndCompletedAtGreaterThanEqual(userId, TaskStatus.DONE, since));
    }

    private TasksSection tasksSection(UUID userId, OffsetDateTime since) {
        List<TaskEntry> completed = taskRepository
                .findTop300ByUserIdAndStatusAndCompletedAtGreaterThanEqualOrderByCompletedAtAsc(userId, TaskStatus.DONE, since).stream()
                .map(t -> new TaskEntry(t.getTitle(), t.getPriority(), t.getCompletedAt()))
                .toList();
        return new TasksSection(taskRepository.countByUserIdAndStatus(userId, TaskStatus.TODO), completed);
    }

    private void validatePeriod(int days) {
        if (days < 1 || days > MAX_PERIOD_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O período precisa ser de 1 a 365 dias.");
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    /** Quais seções entram no relatório. */
    private record Scope(boolean events, boolean checkIns, boolean usage, boolean tasks) {

        static final Scope EVERYTHING = new Scope(true, true, true, true);

        static Scope from(ResponsibleLink link) {
            return new Scope(link.isShareEvents(), link.isShareCheckIns(), link.isShareUsage(), link.isShareTasks());
        }
    }
}
