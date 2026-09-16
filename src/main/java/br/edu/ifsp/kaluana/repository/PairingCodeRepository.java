package br.edu.ifsp.kaluana.repository;

import br.edu.ifsp.kaluana.model.PairingCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;


public interface PairingCodeRepository extends JpaRepository<PairingCode, UUID> {
    Optional<PairingCode> findByCodeAndUsedFalseAndExpiresAtAfter(String code, OffsetDateTime expiresAt);
}