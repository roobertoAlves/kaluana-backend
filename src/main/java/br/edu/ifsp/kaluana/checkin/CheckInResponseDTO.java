package br.edu.ifsp.kaluana.checkin;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckInResponseDTO(
        UUID id,
        LocalDate date,
        int level,
        OffsetDateTime timestamp
) {
    public static CheckInResponseDTO from(CheckIn checkIn) {
        return new CheckInResponseDTO(checkIn.getId(), checkIn.getDay(), checkIn.getLevel(), checkIn.getRecordedAt());
    }
}
