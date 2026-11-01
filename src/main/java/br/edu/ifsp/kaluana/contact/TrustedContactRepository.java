package br.edu.ifsp.kaluana.contact;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrustedContactRepository extends JpaRepository<TrustedContact, UUID> {

    List<TrustedContact> findByUserIdOrderByNameAsc(UUID userId);

    Optional<TrustedContact> findByIdAndUserId(UUID id, UUID userId);
}
