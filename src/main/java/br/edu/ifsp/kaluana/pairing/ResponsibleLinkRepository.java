package br.edu.ifsp.kaluana.pairing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResponsibleLinkRepository extends JpaRepository<ResponsibleLink, UUID>{
    boolean existsByResponsibleIdAndPrimaryUserId(UUID responsibleId, UUID primaryUserId);
}

