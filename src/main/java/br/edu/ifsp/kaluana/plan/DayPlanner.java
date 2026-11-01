package br.edu.ifsp.kaluana.plan;

import br.edu.ifsp.kaluana.task.TaskPriority;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * RF10 / RNF1: monta o plano do dia por regras fixas. Função pura: as mesmas
 * entradas dão sempre o mesmo plano, sem relógio, banco ou serviço externo.
 * As regras estão descritas no Apêndice "Modelo Comportamental" do relatório.
 */
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

    /** Ordem sugerida pelas regras: atrasadas, do dia, e depois conforme o nível do check-in. */
    public List<PlannableTask> order(List<PlannableTask> tasks, LocalDate day, Integer energyLevel) {
        Comparator<PlannableTask> byUrgency = Comparator.comparingInt(task -> urgencyRank(task, day));
        Comparator<PlannableTask> byPriority = Comparator.comparing(PlannableTask::priority).reversed();
        Comparator<PlannableTask> shorterFirst = Comparator.comparingInt(PlannableTask::minutes);

        Comparator<PlannableTask> byEnergy;
        if (energyLevel != null && energyLevel <= 2) {
            // Dia difícil: começar pelo que é curto facilita o primeiro passo.
            byEnergy = shorterFirst.thenComparing(byPriority);
        } else if (energyLevel != null && energyLevel >= 4) {
            byEnergy = byPriority.thenComparing(shorterFirst.reversed());
        } else {
            byEnergy = byPriority.thenComparing(shorterFirst);
        }

        return tasks.stream()
                .sorted(byUrgency.thenComparing(byEnergy)
                        // Desempate estável, para o plano não "pular" entre chamadas.
                        .thenComparing(PlannableTask::title, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PlannableTask::id))
                .toList();
    }

    /** Distribui as tarefas, já ordenadas, na janela [start, end], com pausas entre elas. */
    public Plan layout(List<PlannableTask> ordered, LocalTime start, LocalTime end) {
        List<Block> blocks = new ArrayList<>();
        List<PlannableTask> unscheduled = new ArrayList<>();
        int cursor = minutesOfDay(start);
        int limit = minutesOfDay(end);

        for (PlannableTask task : ordered) {
            boolean needsBreak = !blocks.isEmpty();
            int needed = task.minutes() + (needsBreak ? BREAK_MINUTES : 0);
            if (cursor + needed > limit) {
                // Não cabe: vai para a lista à parte, mas tarefas menores ainda podem caber depois.
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
