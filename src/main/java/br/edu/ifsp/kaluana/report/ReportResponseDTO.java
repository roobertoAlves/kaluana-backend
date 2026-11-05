package br.edu.ifsp.kaluana.report;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.edu.ifsp.kaluana.event.EventCategory;
import br.edu.ifsp.kaluana.pairing.LinkRelationship;
import br.edu.ifsp.kaluana.task.TaskPriority;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReportResponseDTO(
        String userName,
        int periodDays,
        OffsetDateTime generatedAt,
        boolean shared,
        String preparedFor,
        LinkRelationship relationship,
        String disclaimer,
        EventsSection events,
        List<CheckInEntry> checkIns,
        UsageSection usage,
        TasksSection tasks
) {

    public record EventsSection(long total, Map<EventCategory, Long> byCategory, long urgent, List<EventEntry> entries) {
    }

    public record EventEntry(OffsetDateTime occurredAt, EventCategory category, boolean urgent, String note) {
    }

    public record CheckInEntry(LocalDate date, int level) {
    }

    public record UsageSection(
            long timerSessionsDone,
            long timerSessionsCanceled,
            long tasksCompleted,
            long relaxSessions,
            long sleepSessions) {
    }

    public record TasksSection(long pending, List<TaskEntry> completed) {
    }

    public record TaskEntry(String title, TaskPriority priority, OffsetDateTime completedAt) {
    }
}
