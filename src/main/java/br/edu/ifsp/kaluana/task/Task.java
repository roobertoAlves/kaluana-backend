package br.edu.ifsp.kaluana.task;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;


@Entity
@Table(name = "tasks")
public class Task {

    public static final int MAX_STEPS = 12;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private TaskPriority priority;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<TaskStep> steps = new ArrayList<>();

    protected Task() {
    }

    public Task(UUID userId) {
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

    public void describe(String title, TaskPriority priority, Integer estimatedMinutes, LocalDate dueDate) {
        this.title = title;
        this.priority = priority;
        this.estimatedMinutes = estimatedMinutes;
        this.dueDate = dueDate;
    }

    
    public void reschedule(LocalDate newDueDate) {
        if (status == TaskStatus.DONE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A tarefa já foi concluída.");
        }
        this.dueDate = newDueDate;
    }

    public void markDone(boolean done) {
        if (done && status == TaskStatus.TODO) {
            status = TaskStatus.DONE;
            completedAt = OffsetDateTime.now();
        } else if (!done && status == TaskStatus.DONE) {
            status = TaskStatus.TODO;
            completedAt = null;
        }
    }

    public TaskStep addStep(String text) {
        if (steps.size() >= MAX_STEPS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Uma tarefa pode ter até " + MAX_STEPS + " microetapas.");
        }
        int nextPosition = steps.stream().mapToInt(TaskStep::getPosition).max().orElse(-1) + 1;
        TaskStep step = new TaskStep(this, text, nextPosition);
        steps.add(step);
        return step;
    }

    public void updateStep(UUID stepId, String text, boolean done) {
        findStep(stepId).update(text, done);
    }

    public void removeStep(UUID stepId) {
        steps.remove(findStep(stepId));
    }

    private TaskStep findStep(UUID stepId) {
        return steps.stream()
                .filter(step -> stepId.equals(step.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Microetapa não encontrada."));
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

    public TaskPriority getPriority() {
        return priority;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public List<TaskStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }
}
