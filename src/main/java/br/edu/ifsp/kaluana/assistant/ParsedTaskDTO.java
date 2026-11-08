package br.edu.ifsp.kaluana.assistant;

import br.edu.ifsp.kaluana.task.TaskPriority;

import java.time.LocalDate;
import java.util.List;

public record ParsedTaskDTO(
        String title,
        LocalDate dueDate,
        Integer estimatedMinutes,
        TaskPriority priority,
        List<String> steps
) {
}
