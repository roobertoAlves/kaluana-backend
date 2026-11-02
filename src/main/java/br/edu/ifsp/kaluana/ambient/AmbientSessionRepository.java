package br.edu.ifsp.kaluana.ambient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface AmbientSessionRepository extends JpaRepository<AmbientSession, UUID> {

    List<AmbientSession> findTop100ByUserIdOrderByFinishedAtDesc(UUID userId);

    long countByUserIdAndModeAndFinishedAtGreaterThanEqual(UUID userId, AmbientMode mode, OffsetDateTime since);
}
