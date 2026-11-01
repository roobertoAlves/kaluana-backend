package br.edu.ifsp.kaluana.access;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccessSettingsRepository extends JpaRepository<AccessSettings, UUID> {
}
