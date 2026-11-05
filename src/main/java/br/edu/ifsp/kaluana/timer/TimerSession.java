package br.edu.ifsp.kaluana.timer;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "timer_sessions")
public class TimerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(nullable = false, length = 80)
    private String task;

    @Column(name = "planned_minutes", nullable = false)
    private int plannedMinutes;

    @Column(name = "steps_count", nullable = false)
    private short stepsCount;

    @Column(nullable = false)
    private boolean water;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private TimerSound sound = TimerSound.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TimerSessionStatus status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at", nullable = false)
    private OffsetDateTime finishedAt;

    protected TimerSession() {
    }

    public TimerSession(UUID userId, UUID taskId, String task, int plannedMinutes, int stepsCount, boolean water,
                        TimerSound sound, TimerSessionStatus status, OffsetDateTime startedAt, OffsetDateTime finishedAt) {
        this.userId = userId;
        this.taskId = taskId;
        this.task = task;
        this.plannedMinutes = plannedMinutes;
        this.stepsCount = (short) stepsCount;
        this.water = water;
        this.sound = sound;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public TimerSound getSound() {
        return sound;
    }

    public String getTask() {
        return task;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public boolean isWater() {
        return water;
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
