package br.edu.ifsp.kaluana.plan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import br.edu.ifsp.kaluana.plan.DayPlanResponseDTO.Source;
import br.edu.ifsp.kaluana.plan.DayPlanner.Block;
import br.edu.ifsp.kaluana.plan.DayPlanner.BlockKind;
import br.edu.ifsp.kaluana.plan.DayPlanner.Plan;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "day_plans")
public class DayPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "plan_date", nullable = false, updatable = false)
    private LocalDate planDate;

    @Column(name = "window_end", nullable = false)
    private LocalTime windowEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 9)
    private Source source;

    @Column(name = "energy_level")
    private Integer energyLevel;

    @Column(length = 200)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "day_plan_blocks", joinColumns = @JoinColumn(name = "plan_id"))
    @OrderColumn(name = "position")
    private List<PlannedBlock> blocks = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "day_plan_unscheduled", joinColumns = @JoinColumn(name = "plan_id"))
    @OrderColumn(name = "position")
    private List<UnscheduledTask> unscheduled = new ArrayList<>();

    protected DayPlan() {
    }

    public DayPlan(UUID userId, LocalDate planDate) {
        this.userId = userId;
        this.planDate = planDate;
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

    public void replace(Plan plan, LocalTime windowEnd, Source source, Integer energyLevel, String note) {
        this.windowEnd = windowEnd;
        this.source = source;
        this.energyLevel = energyLevel;
        this.note = note;
        this.blocks.clear();
        plan.blocks().forEach(b -> blocks.add(new PlannedBlock(b.kind(), b.taskId(), b.title(), b.start(), b.end())));
        this.unscheduled.clear();
        plan.unscheduled().forEach(t -> unscheduled.add(new UnscheduledTask(t.id(), t.title(), t.minutes())));
    }

    public List<Block> blocksAsPlanned() {
        return blocks.stream().map(PlannedBlock::toBlock).toList();
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public LocalTime getWindowEnd() {
        return windowEnd;
    }

    public Source getSource() {
        return source;
    }

    public Integer getEnergyLevel() {
        return energyLevel;
    }

    public String getNote() {
        return note;
    }

    public List<UnscheduledTask> getUnscheduled() {
        return Collections.unmodifiableList(unscheduled);
    }

    @Embeddable
    public static class PlannedBlock {

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 5)
        private BlockKind kind;

        @Column(name = "task_id")
        private UUID taskId;

        @Column(nullable = false, length = 120)
        private String title;

        @Column(name = "start_time", nullable = false)
        private LocalTime startTime;

        @Column(name = "end_time", nullable = false)
        private LocalTime endTime;

        protected PlannedBlock() {
        }

        PlannedBlock(BlockKind kind, UUID taskId, String title, LocalTime startTime, LocalTime endTime) {
            this.kind = kind;
            this.taskId = taskId;
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        Block toBlock() {
            return new Block(kind, taskId, title, startTime, endTime);
        }
    }

    @Embeddable
    public static class UnscheduledTask {

        @Column(name = "task_id", nullable = false)
        private UUID taskId;

        @Column(nullable = false, length = 120)
        private String title;

        @Column(nullable = false)
        private int minutes;

        protected UnscheduledTask() {
        }

        UnscheduledTask(UUID taskId, String title, int minutes) {
            this.taskId = taskId;
            this.title = title;
            this.minutes = minutes;
        }

        public UUID getTaskId() {
            return taskId;
        }

        public String getTitle() {
            return title;
        }

        public int getMinutes() {
            return minutes;
        }
    }
}
