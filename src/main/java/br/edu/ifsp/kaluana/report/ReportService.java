package br.edu.ifsp.kaluana.report;

import br.edu.ifsp.kaluana.access.AccessSettingsService;
import br.edu.ifsp.kaluana.pairing.PairingService;

import br.edu.ifsp.kaluana.report.ReportResponseDTO.CheckInEntry;
import br.edu.ifsp.kaluana.report.ReportResponseDTO.EventsSection;
import br.edu.ifsp.kaluana.report.ReportResponseDTO.UsageSection;
import br.edu.ifsp.kaluana.access.AccessSettings;
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
 * Para o responsável, cada seção só entra se o usuário autorizou (RF8/RNF3).
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
    private final AccessSettingsService accessSettingsService;
    private final PairingService pairingService;

    public ReportService(
            UserRepository userRepository,
            LifeEventRepository lifeEventRepository,
            CheckInRepository checkInRepository,
            TimerSessionRepository timerSessionRepository,
            TaskRepository taskRepository,
            AccessSettingsService accessSettingsService,
            PairingService pairingService) {
        this.userRepository = userRepository;
        this.lifeEventRepository = lifeEventRepository;
        this.checkInRepository = checkInRepository;
        this.timerSessionRepository = timerSessionRepository;
        this.taskRepository = taskRepository;
        this.accessSettingsService = accessSettingsService;
        this.pairingService = pairingService;
    }

    /** Relatório da própria pessoa: todas as seções. */
    @Transactional(readOnly = true)
    public ReportResponseDTO ownReport(UUID userId, int days) {
        return build(userId, days, Scope.EVERYTHING, false);
    }

    /** Relatório de quem está emparelhado com o responsável, filtrado pelo consentimento. */
    @Transactional(readOnly = true)
    public ReportResponseDTO linkedReport(UUID responsibleId, UUID primaryUserId, int days) {
        if (!pairingService.isLinked(responsibleId, primaryUserId)) {
            // 404 e não 403: não confirma para terceiros que a pessoa existe.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vínculo não encontrado.");
        }

        AccessSettings access = accessSettingsService.find(primaryUserId);
        if (!access.isShareReports()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "A pessoa ainda não liberou relatórios para você.");
        }

        return build(primaryUserId, days, Scope.from(access), true);
    }

    private ReportResponseDTO build(UUID userId, int days, Scope scope, boolean shared) {
        validatePeriod(days);
        User user = findUser(userId);
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime since = now.minusDays(days);

        return new ReportResponseDTO(
                user.getName(),
                days,
                now,
                shared,
                DISCLAIMER,
                scope.events() ? eventsSection(userId, since) : null,
                scope.checkIns() ? checkInEntries(userId, since.toLocalDate()) : null,
                scope.usage() ? usageSection(userId, since) : null);
    }

    private EventsSection eventsSection(UUID userId, OffsetDateTime since) {
        Map<EventCategory, Long> byCategory = new EnumMap<>(EventCategory.class);
        lifeEventRepository.countByCategorySince(userId, since)
                .forEach(count -> byCategory.put(count.getCategory(), count.getTotal()));

        long total = byCategory.values().stream().mapToLong(Long::longValue).sum();
        long urgent = lifeEventRepository.countByUserIdAndUrgentTrueAndOccurredAtGreaterThanEqual(userId, since);
        return new EventsSection(total, byCategory, urgent);
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
    private record Scope(boolean events, boolean checkIns, boolean usage) {

        static final Scope EVERYTHING = new Scope(true, true, true);

        static Scope from(AccessSettings access) {
            return new Scope(access.isShareEvents(), access.isShareCheckIns(), access.isShareUsage());
        }
    }
}
