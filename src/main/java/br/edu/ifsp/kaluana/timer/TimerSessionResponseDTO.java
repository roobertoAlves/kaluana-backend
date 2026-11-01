package br.edu.ifsp.kaluana.timer;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TimerSessionResponseDTO(
        UUID id,
        UUID taskId,
        String task,
        int plannedMinutes,
        int stepsCount,
        boolean water,
        TimerSound sound,
        TimerSessionStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {
    public static TimerSessionResponseDTO from(TimerSession session) {
        return new TimerSessionResponseDTO(
                session.getId(),
                session.getTaskId(),
                session.getTask(),
                session.getPlannedMinutes(),
                session.getStepsCount(),
                session.isWater(),
                session.getSound(),
                session.getStatus(),
                session.getStartedAt(),
                session.getFinishedAt());
    }
}
