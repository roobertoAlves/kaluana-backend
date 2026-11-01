package br.edu.ifsp.kaluana.plan;

import br.edu.ifsp.kaluana.assistant.AssistantService;
import br.edu.ifsp.kaluana.assistant.PlanningAssistant.TaskSummary;
import br.edu.ifsp.kaluana.checkin.CheckInService;
import br.edu.ifsp.kaluana.plan.DayPlanResponseDTO.Source;
import br.edu.ifsp.kaluana.plan.DayPlanner.Plan;
import br.edu.ifsp.kaluana.plan.DayPlanner.PlannableTask;
import br.edu.ifsp.kaluana.task.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * RF10/RF11: junta tarefas pendentes e check-in do dia, ordena (pelas regras ou
 * pelo assistente) e distribui na janela de horários. O plano não é salvo.
 */
@Service
public class DayPlanService {

    private static final String ASSISTANT_FALLBACK_NOTE =
            "O assistente não pôde ser usado agora; o plano foi montado pelas regras.";

    private final TaskService taskService;
    private final CheckInService checkInService;
    private final AssistantService assistantService;
    private final DayPlanner planner;

    public DayPlanService(TaskService taskService, CheckInService checkInService,
                          AssistantService assistantService, DayPlanner planner) {
        this.taskService = taskService;
        this.checkInService = checkInService;
        this.assistantService = assistantService;
        this.planner = planner;
    }

    public DayPlanResponseDTO plan(UUID userId, LocalDate day, LocalTime start, LocalTime end, boolean useAssistant) {
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário de início precisa ser antes do fim.");
        }

        List<PlannableTask> tasks = taskService.plannableFor(userId, day).stream()
                .map(t -> new PlannableTask(t.getId(), t.getTitle(), t.getPriority(), t.getEstimatedMinutes(), t.getDueDate()))
                .toList();
        Integer energy = checkInService.levelOn(userId, day).orElse(null);
        List<PlannableTask> ordered = planner.order(tasks, day, energy);

        Source source = Source.RULES;
        String note = null;
        if (useAssistant) {
            Optional<List<PlannableTask>> suggested = orderWithAssistant(userId, ordered, energy);
            if (suggested.isPresent()) {
                ordered = suggested.get();
                source = Source.ASSISTANT;
            } else if (!ordered.isEmpty()) {
                note = ASSISTANT_FALLBACK_NOTE;
            }
        }

        Plan plan = planner.layout(ordered, start, end);
        return new DayPlanResponseDTO(
                day,
                energy,
                source,
                note,
                plan.blocks().stream()
                        .map(b -> new DayPlanResponseDTO.Block(b.kind(), b.taskId(), b.title(), b.start(), b.end()))
                        .toList(),
                plan.unscheduled().stream()
                        .map(t -> new DayPlanResponseDTO.Unscheduled(t.id(), t.title(), t.minutes()))
                        .toList());
    }

    /**
     * A IA só reordena: recebe título e duração (RNF7) e devolve índices. Tarefas
     * que ela omitir entram no fim, na ordem das regras, para nenhuma sumir.
     */
    private Optional<List<PlannableTask>> orderWithAssistant(UUID userId, List<PlannableTask> ruleOrder, Integer energy) {
        List<TaskSummary> summaries = ruleOrder.stream()
                .map(t -> new TaskSummary(t.title(), t.minutes()))
                .toList();

        return assistantService.suggestOrder(userId, summaries, energy).map(indexes -> {
            Set<PlannableTask> result = new LinkedHashSet<>();
            indexes.forEach(i -> result.add(ruleOrder.get(i)));
            result.addAll(ruleOrder);
            return new ArrayList<>(result);
        });
    }
}
