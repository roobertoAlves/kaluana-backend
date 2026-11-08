package br.edu.ifsp.kaluana.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "access_logs")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "primary_user_id", nullable = false, updatable = false)
    private UUID primaryUserId;

    @Column(name = "viewer_id", nullable = false, updatable = false)
    private UUID viewerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 12)
    private AccessAction action;

    @Column(name = "period_days", nullable = false, updatable = false)
    private int periodDays;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AccessLog() {
    }

    public AccessLog(UUID primaryUserId, UUID viewerId, AccessAction action, int periodDays) {
        this.primaryUserId = primaryUserId;
        this.viewerId = viewerId;
        this.action = action;
        this.periodDays = periodDays;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getViewerId() {
        return viewerId;
    }

    public AccessAction getAction() {
        return action;
    }

    public int getPeriodDays() {
        return periodDays;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
