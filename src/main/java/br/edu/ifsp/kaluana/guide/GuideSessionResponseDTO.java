package br.edu.ifsp.kaluana.guide;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GuideSessionResponseDTO(
        UUID id,
        String guideId,
        int guideVersion,
        GuideSessionStatus status,
        int stepsCount,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt
) {
    public static GuideSessionResponseDTO from(GuideSession session) {
        return new GuideSessionResponseDTO(session.getId(), session.getGuideId(), session.getGuideVersion(), session.getStatus(),
                session.getStepsCount(), session.getStartedAt(), session.getFinishedAt());
    }
}
