package br.edu.ifsp.kaluana.task;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_steps")
public class TaskStep {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, length = 120)
    private String text;

    @Column(nullable = false)
    private short position;

    @Column(nullable = false)
    private boolean done;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected TaskStep() {
    }

    TaskStep(Task task, String text, int position) {
        this.task = task;
        this.text = text;
        this.position = (short) position;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    void update(String text, boolean done) {
        this.text = text;
        this.done = done;
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }

    public boolean isDone() {
        return done;
    }
}
