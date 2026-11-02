package br.edu.ifsp.kaluana.plan;

import br.edu.ifsp.kaluana.plan.DayPlanner.Block;
import br.edu.ifsp.kaluana.plan.DayPlanner.BlockKind;
import br.edu.ifsp.kaluana.plan.DayPlanner.Plan;
import br.edu.ifsp.kaluana.plan.DayPlanner.PlannableTask;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * RF14 / RNF1: reorganiza um plano salvo quando o dia muda. Função pura, como
 * o {@link DayPlanner}: recebe o plano anterior, as tarefas pendentes e o
 * horário atual (vindo do aparelho) e devolve o novo plano e o que mudou.
 * As regras estão no Apêndice "Modelo Comportamental" do relatório.
 */
@Component
public class DayReplanner {

    private static final int SLOT_MINUTES = 5;
    private static final int LAST_SLOT = 23 * 60 + 55;

    public enum ChangeKind { MOVED, ADDED, DONE, REMOVED, DROPPED }

    /** {@code from} é o horário no plano anterior; {@code to}, no novo. */
    public record Change(UUID taskId, String title, ChangeKind kind, LocalTime from, LocalTime to) {
    }

    public record Replan(Plan plan, List<Change> changes) {
    }

    private final DayPlanner planner;

    public DayReplanner(DayPlanner planner) {
        this.planner = planner;
    }

    /**
     * @param previous blocos do plano salvo
     * @param pending  tarefas pendentes que entram no dia (já atualizadas)
     * @param doneIds  tarefas do plano anterior que foram concluídas
     */
    public Replan replan(List<Block> previous, List<PlannableTask> pending, Set<UUID> doneIds,
                         LocalDate day, Integer energyLevel, LocalTime now, LocalTime end) {
        Map<UUID, PlannableTask> pendingById = new LinkedHashMap<>();
        pending.forEach(task -> pendingById.put(task.id(), task));

        // O bloco em andamento não se move: quem está fazendo a tarefa não deve ser interrompido.
        Block current = previous.stream()
                .filter(b -> b.kind() == BlockKind.TASK && pendingById.containsKey(b.taskId()))
                .filter(b -> !now.isBefore(b.start()) && now.isBefore(b.end()))
                .findFirst()
                .map(b -> new Block(BlockKind.TASK, b.taskId(), pendingById.get(b.taskId()).title(), b.start(), b.end()))
                .orElse(null);

        // A ordem que o usuário já aceitou é mantida; o que não estava em bloco vem depois, pelas regras.
        List<PlannableTask> ordered = new ArrayList<>();
        Set<UUID> placed = new HashSet<>();
        if (current != null) {
            placed.add(current.taskId());
        }
        for (Block b : previous) {
            PlannableTask task = b.kind() == BlockKind.TASK ? pendingById.get(b.taskId()) : null;
            if (task != null && placed.add(task.id())) {
                ordered.add(task);
            }
        }
        List<PlannableTask> rest = pending.stream().filter(t -> !placed.contains(t.id())).toList();
        ordered.addAll(planner.order(rest, day, energyLevel));

        LocalTime start = current != null ? current.end() : nextSlot(now);
        Plan plan = planner.layout(current != null ? List.of(current) : List.of(), ordered, start, end);
        return new Replan(plan, changes(previous, plan, pendingById, doneIds, now));
    }

    private static List<Change> changes(List<Block> previous, Plan plan, Map<UUID, PlannableTask> pendingById,
                                        Set<UUID> doneIds, LocalTime now) {
        Map<UUID, LocalTime> newStart = new HashMap<>();
        plan.blocks().stream()
                .filter(b -> b.kind() == BlockKind.TASK)
                .forEach(b -> newStart.put(b.taskId(), b.start()));

        List<Change> changes = new ArrayList<>();
        Set<UUID> before = new HashSet<>();
        for (Block b : previous) {
            if (b.kind() != BlockKind.TASK || b.taskId() == null || !before.add(b.taskId())) continue;
            UUID id = b.taskId();
            PlannableTask task = pendingById.get(id);
            if (task == null) {
                // Blocos que já passaram e foram feitos não são novidade.
                if (b.end().isAfter(now)) {
                    changes.add(new Change(id, b.title(), doneIds.contains(id) ? ChangeKind.DONE : ChangeKind.REMOVED, b.start(), null));
                }
            } else if (!newStart.containsKey(id)) {
                changes.add(new Change(id, task.title(), ChangeKind.DROPPED, b.start(), null));
            } else if (!Objects.equals(newStart.get(id), b.start())) {
                changes.add(new Change(id, task.title(), ChangeKind.MOVED, b.start(), newStart.get(id)));
            }
        }
        plan.blocks().stream()
                .filter(b -> b.kind() == BlockKind.TASK && !before.contains(b.taskId()))
                .forEach(b -> changes.add(new Change(b.taskId(), b.title(), ChangeKind.ADDED, null, b.start())));
        return changes;
    }

    /** Próximo horário múltiplo de 5 minutos ("09:07" → "09:10"), como o aplicativo. */
    static LocalTime nextSlot(LocalTime now) {
        int minutes = now.getHour() * 60 + now.getMinute() + (now.getSecond() > 0 ? 1 : 0);
        int slot = Math.min((minutes + SLOT_MINUTES - 1) / SLOT_MINUTES * SLOT_MINUTES, LAST_SLOT);
        return LocalTime.of(slot / 60, slot % 60);
    }
}
