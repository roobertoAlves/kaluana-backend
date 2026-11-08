package br.edu.ifsp.kaluana.access;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {

    List<AccessLog> findTop100ByPrimaryUserIdOrderByCreatedAtDesc(UUID primaryUserId);
}
