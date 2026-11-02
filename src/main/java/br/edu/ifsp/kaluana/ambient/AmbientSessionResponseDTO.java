package br.edu.ifsp.kaluana.ambient;

import br.edu.ifsp.kaluana.timer.TimerSessionStatus;
import br.edu.ifsp.kaluana.timer.TimerSound;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AmbientSessionResponseDTO(
        UUID id,
        AmbientMode mode,
        TimerSound sound,
        int plannedMinutes,
        TimerSessionStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {
    public static AmbientSessionResponseDTO from(AmbientSession session) {
        return new AmbientSessionResponseDTO(
                session.getId(),
                session.getMode(),
                session.getSound(),
                session.getPlannedMinutes(),
                session.getStatus(),
                session.getStartedAt(),
                session.getFinishedAt());
    }
}
