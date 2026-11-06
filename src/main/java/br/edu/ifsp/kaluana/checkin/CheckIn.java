package br.edu.ifsp.kaluana.checkin;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_ins")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private LocalDate day;

    @Column(nullable = false)
    private short level;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    protected CheckIn() {
    }

    public CheckIn(UUID userId, LocalDate day) {
        this.userId = userId;
        this.day = day;
    }

    public void record(int level) {
        this.level = (short) level;
        this.recordedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDate getDay() {
        return day;
    }

    public int getLevel() {
        return level;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
