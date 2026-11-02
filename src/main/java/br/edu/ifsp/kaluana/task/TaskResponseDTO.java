package br.edu.ifsp.kaluana.task;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        TaskPriority priority,
        Integer estimatedMinutes,
        LocalDate dueDate,
        boolean done,
        OffsetDateTime completedAt,
        List<Step> steps
) {

    public record Step(UUID id, String text, boolean done) {
    }

    public static TaskResponseDTO from(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getPriority(),
                task.getEstimatedMinutes(),
                task.getDueDate(),
                task.getStatus() == TaskStatus.DONE,
                task.getCompletedAt(),
                task.getSteps().stream()
                        .map(step -> new Step(step.getId(), step.getText(), step.isDone()))
                        .toList());
    }
}
