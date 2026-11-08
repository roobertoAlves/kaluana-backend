package br.edu.ifsp.kaluana.guide;

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
@Table(name = "guide_sessions")
public class GuideSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "guide_id", nullable = false, updatable = false, length = 40)
    private String guideId;

    @Column(name = "guide_version", nullable = false, updatable = false)
    private int guideVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 10)
    private GuideSessionStatus status;

    @Column(name = "steps_count", nullable = false, updatable = false)
    private int stepsCount;

    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "finished_at", nullable = false, updatable = false)
    private OffsetDateTime finishedAt;

    protected GuideSession() {
    }

    public GuideSession(UUID userId, String guideId, int guideVersion, GuideSessionStatus status, int stepsCount,
                        OffsetDateTime startedAt, OffsetDateTime finishedAt) {
        this.userId = userId;
        this.guideId = guideId;
        this.guideVersion = guideVersion;
        this.status = status;
        this.stepsCount = stepsCount;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getGuideId() {
        return guideId;
    }

    public int getGuideVersion() {
        return guideVersion;
    }

    public GuideSessionStatus getStatus() {
        return status;
    }

    public int getStepsCount() {
        return stepsCount;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }
}
