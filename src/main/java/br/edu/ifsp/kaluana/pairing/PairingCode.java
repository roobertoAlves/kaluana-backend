package br.edu.ifsp.kaluana.pairing;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "pairing_codes")
public class PairingCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 12)
    private String code;

    @Column(name = "primary_user_id", nullable = false)
    private UUID primaryUserId;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected PairingCode() {
    }

    public PairingCode(String code, UUID primaryUserId, OffsetDateTime expiresAt) {
        this.code = code;
        this.primaryUserId = primaryUserId;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public UUID getPrimaryUserId() {
        return primaryUserId;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void markAsUsed() {
        this.used = true;
    }
}
