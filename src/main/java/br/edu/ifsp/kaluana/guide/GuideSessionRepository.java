package br.edu.ifsp.kaluana.guide;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GuideSessionRepository extends JpaRepository<GuideSession, UUID> {

    List<GuideSession> findTop50ByUserIdOrderByFinishedAtDesc(UUID userId);
}
