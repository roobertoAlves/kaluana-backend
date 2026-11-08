package br.edu.ifsp.kaluana.access;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AccessLogDTO(
        UUID viewerId,
        String viewerName,
        AccessAction action,
        int periodDays,
        OffsetDateTime at
) {
}
