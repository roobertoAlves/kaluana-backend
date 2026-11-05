package br.edu.ifsp.kaluana.plan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.ifsp.kaluana.task.TaskPriority;

@Component
public class DayPlanner {

    public static final int DEFAULT_TASK_MINUTES = 25;
    public static final int BREAK_MINUTES = 5;

    public record PlannableTask(UUID id, String title, TaskPriority priority, Integer estimatedMinutes, LocalDate dueDate) {

        public int minutes() {
            return estimatedMinutes != null ? estimatedMinutes : DEFAULT_TASK_MINUTES;
        }
    }

    public enum BlockKind { TASK, BREAK }

    public record Block(BlockKind kind, UUID taskId, String title, LocalTime start, LocalTime end) {
    }

    public record Plan(List<Block> blocks, List<PlannableTask> unscheduled) {
    }

    public List<PlannableTask> order(List<PlannableTask> tasks, LocalDate day, Integer energyLevel) {
        Comparator<PlannableTask> byUrgency = Comparator.comparingInt(task -> urgencyRank(task, day));
        Comparator<PlannableTask> byPriority = Comparator.comparing(PlannableTask::priority).reversed();
        Comparator<PlannableTask> shorterFirst = Comparator.comparingInt(PlannableTask::minutes);

        Comparator<PlannableTask> byEnergy;
        if (energyLevel != null && energyLevel <= 2) {
            byEnergy = shorterFirst.thenComparing(byPriority);
        } else if (energyLevel != null && energyLevel >= 4) {
            byEnergy = byPriority.thenComparing(shorterFirst.reversed());
        } else {
            byEnergy = byPriority.thenComparing(shorterFirst);
        }

        return tasks.stream()
                .sorted(byUrgency.thenComparing(byEnergy)
                        .thenComparing(PlannableTask::title, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PlannableTask::id))
                .toList();
    }

    public Plan layout(List<PlannableTask> ordered, LocalTime start, LocalTime end) {
        return layout(List.of(), ordered, start, end);
    }

    public Plan layout(List<Block> fixed, List<PlannableTask> ordered, LocalTime start, LocalTime end) {
        List<Block> blocks = new ArrayList<>(fixed);
        List<PlannableTask> unscheduled = new ArrayList<>();
        int cursor = minutesOfDay(start);
        int limit = minutesOfDay(end);

        for (PlannableTask task : ordered) {
            boolean needsBreak = !blocks.isEmpty();
            int needed = task.minutes() + (needsBreak ? BREAK_MINUTES : 0);
            if (cursor + needed > limit) {
                unscheduled.add(task);
                continue;
            }
            if (needsBreak) {
                blocks.add(new Block(BlockKind.BREAK, null, "Pausa", toTime(cursor), toTime(cursor + BREAK_MINUTES)));
                cursor += BREAK_MINUTES;
            }
            blocks.add(new Block(BlockKind.TASK, task.id(), task.title(), toTime(cursor), toTime(cursor + task.minutes())));
            cursor += task.minutes();
        }
        return new Plan(blocks, unscheduled);
    }

    private static int urgencyRank(PlannableTask task, LocalDate day) {
        if (task.dueDate() == null) return 2;
        if (task.dueDate().isBefore(day)) return 0;
        return task.dueDate().isEqual(day) ? 1 : 2;
    }

    private static int minutesOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private static LocalTime toTime(int minutesOfDay) {
        return LocalTime.of(minutesOfDay / 60, minutesOfDay % 60);
    }
}
