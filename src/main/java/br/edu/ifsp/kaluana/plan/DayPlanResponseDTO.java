package br.edu.ifsp.kaluana.plan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.edu.ifsp.kaluana.plan.DayPlanner.BlockKind;
import br.edu.ifsp.kaluana.plan.DayReplanner.ChangeKind;

public record DayPlanResponseDTO(
        LocalDate date,
        Integer energyLevel,
        Source source,
        String note,
        @JsonFormat(pattern = "HH:mm") LocalTime windowEnd,
        List<Block> blocks,
        List<Unscheduled> unscheduled,
        List<Change> changes
) {

    public enum Source { RULES, ASSISTANT }

    public record Block(
            BlockKind kind,
            UUID taskId,
            String title,
            @JsonFormat(pattern = "HH:mm") LocalTime start,
            @JsonFormat(pattern = "HH:mm") LocalTime end) {

        static Block from(DayPlanner.Block block) {
            return new Block(block.kind(), block.taskId(), block.title(), block.start(), block.end());
        }
    }

    public record Unscheduled(UUID taskId, String title, int minutes) {
    }

    public record Change(
            UUID taskId,
            String title,
            ChangeKind kind,
            @JsonFormat(pattern = "HH:mm") LocalTime from,
            @JsonFormat(pattern = "HH:mm") LocalTime to) {

        static Change from(DayReplanner.Change change) {
            return new Change(change.taskId(), change.title(), change.kind(), change.from(), change.to());
        }
    }

    static DayPlanResponseDTO from(DayPlan plan, List<DayReplanner.Change> changes) {
        return new DayPlanResponseDTO(
                plan.getPlanDate(),
                plan.getEnergyLevel(),
                plan.getSource(),
                plan.getNote(),
                plan.getWindowEnd(),
                plan.blocksAsPlanned().stream().map(Block::from).toList(),
                plan.getUnscheduled().stream()
                        .map(t -> new Unscheduled(t.getTaskId(), t.getTitle(), t.getMinutes()))
                        .toList(),
                changes.stream().map(Change::from).toList());
    }
}
