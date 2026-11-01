package br.edu.ifsp.kaluana.timer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TimerSessionRepository extends JpaRepository<TimerSession, UUID> {

    List<TimerSession> findTop200ByUserIdOrderByFinishedAtDesc(UUID userId);

    long countByUserIdAndStatusAndFinishedAtGreaterThanEqual(UUID userId, TimerSessionStatus status, OffsetDateTime since);
}
