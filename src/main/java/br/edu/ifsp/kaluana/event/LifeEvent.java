package br.edu.ifsp.kaluana.event;

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
@Table(name = "life_events")
public class LifeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventCategory category;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private boolean urgent;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LifeEvent() {
        // exigido pelo JPA
    }

    public LifeEvent(UUID userId, EventCategory category, String note, boolean urgent, OffsetDateTime occurredAt) {
        this.userId = userId;
        this.category = category;
        this.note = note;
        this.urgent = urgent;
        this.occurredAt = occurredAt;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public EventCategory getCategory() {
        return category;
    }

    public String getNote() {
        return note;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
