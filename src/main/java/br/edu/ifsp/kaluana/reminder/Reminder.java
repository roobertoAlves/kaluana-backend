package br.edu.ifsp.kaluana.reminder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * RF2: só a configuração do lembrete fica no servidor. O disparo da
 * notificação é agendado no aparelho (RNF2), que funciona offline.
 */
@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime time;

    @Column(name = "lead_minutes", nullable = false)
    private int leadMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReminderFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReminderInsistence insistence;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Reminder() {
        // exigido pelo JPA
    }

    public Reminder(UUID userId) {
        this.userId = userId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void configure(String title, LocalTime time, int leadMinutes, ReminderFrequency frequency,
                          ReminderInsistence insistence, boolean enabled) {
        this.title = title;
        this.time = time;
        this.leadMinutes = leadMinutes;
        this.frequency = frequency;
        this.insistence = insistence;
        this.enabled = enabled;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public LocalTime getTime() {
        return time;
    }

    public int getLeadMinutes() {
        return leadMinutes;
    }

    public ReminderFrequency getFrequency() {
        return frequency;
    }

    public ReminderInsistence getInsistence() {
        return insistence;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
