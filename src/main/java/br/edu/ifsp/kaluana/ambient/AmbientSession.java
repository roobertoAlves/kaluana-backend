package br.edu.ifsp.kaluana.ambient;

import br.edu.ifsp.kaluana.timer.TimerSessionStatus;
import br.edu.ifsp.kaluana.timer.TimerSound;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "ambient_sessions")
public class AmbientSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private AmbientMode mode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private TimerSound sound;

    @Column(name = "planned_minutes", nullable = false)
    private int plannedMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TimerSessionStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at", nullable = false)
    private OffsetDateTime finishedAt;

    protected AmbientSession() {
    }

    public AmbientSession(UUID userId, AmbientMode mode, TimerSound sound, int plannedMinutes,
                          TimerSessionStatus status, OffsetDateTime startedAt, OffsetDateTime finishedAt) {
        this.userId = userId;
        this.mode = mode;
        this.sound = sound;
        this.plannedMinutes = plannedMinutes;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public UUID getId() {
        return id;
    }

    public AmbientMode getMode() {
        return mode;
    }

    public TimerSound getSound() {
        return sound;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public TimerSessionStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }
}
