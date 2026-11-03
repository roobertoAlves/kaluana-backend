package br.edu.ifsp.kaluana.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Token de renovação de sessão. Só o hash SHA-256 fica no banco. */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, updatable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected RefreshToken() {
        // exigido pelo JPA
    }

    public RefreshToken(UUID userId, String tokenHash, OffsetDateTime expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public boolean isUsable(OffsetDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke(OffsetDateTime now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }

    public UUID getUserId() {
        return userId;
    }
}
