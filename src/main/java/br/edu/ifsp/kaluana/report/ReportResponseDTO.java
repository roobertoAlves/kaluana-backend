package br.edu.ifsp.kaluana.report;

import br.edu.ifsp.kaluana.event.EventCategory;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * RF7: relatório só com contagens e listas do que o usuário registrou.
 * Seções não autorizadas (RF8/RNF3) ficam nulas e não aparecem no JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReportResponseDTO(
        String userName,
        int periodDays,
        OffsetDateTime generatedAt,
        boolean shared,
        String disclaimer,
        EventsSection events,
        List<CheckInEntry> checkIns,
        UsageSection usage
) {

    public record EventsSection(long total, Map<EventCategory, Long> byCategory, long urgent) {
    }

    public record CheckInEntry(LocalDate date, int level) {
    }

    public record UsageSection(
            long timerSessionsDone,
            long timerSessionsCanceled,
            long tasksCompleted) {
    }
}
