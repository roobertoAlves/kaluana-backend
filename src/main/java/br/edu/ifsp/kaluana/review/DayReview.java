package br.edu.ifsp.kaluana.review;

import br.edu.ifsp.kaluana.task.ReviewAction;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "day_reviews")
public class DayReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "review_date", nullable = false, updatable = false)
    private LocalDate reviewDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 4)
    private DayFeeling feeling;

    @Column(name = "moved_tomorrow", nullable = false)
    private int movedTomorrow;

    @Column(nullable = false)
    private int unscheduled;

    @Column(nullable = false)
    private int completed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected DayReview() {
    }

    public DayReview(UUID userId, LocalDate reviewDate) {
        this.userId = userId;
        this.reviewDate = reviewDate;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    
    public void record(ReviewAction action) {
        switch (action) {
            case TOMORROW -> movedTomorrow++;
            case NO_DATE -> unscheduled++;
            case DONE -> completed++;
        }
    }

    public void describe(DayFeeling feeling) {
        if (feeling != null) {
            this.feeling = feeling;
        }
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public DayFeeling getFeeling() {
        return feeling;
    }

    public int getMovedTomorrow() {
        return movedTomorrow;
    }

    public int getUnscheduled() {
        return unscheduled;
    }

    public int getCompleted() {
        return completed;
    }
}
