package br.edu.ifsp.kaluana.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    
    @Modifying
    @Query("update EmailVerificationToken t set t.usedAt = :now where t.userId = :userId and t.usedAt is null")
    int invalidateAllOf(@Param("userId") UUID userId, @Param("now") OffsetDateTime now);
}
