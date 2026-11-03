package br.edu.ifsp.kaluana.plan;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.kaluana.assistant.AssistantService;
import br.edu.ifsp.kaluana.assistant.PlanningAssistant.TaskSummary;
import br.edu.ifsp.kaluana.checkin.CheckInService;
import br.edu.ifsp.kaluana.plan.DayPlanResponseDTO.Source;
import br.edu.ifsp.kaluana.plan.DayPlanner.BlockKind;
import br.edu.ifsp.kaluana.plan.DayPlanner.Plan;
import br.edu.ifsp.kaluana.plan.DayPlanner.PlannableTask;
import br.edu.ifsp.kaluana.plan.DayReplanner.Replan;
import br.edu.ifsp.kaluana.task.TaskService;

@Service
public class DayPlanService {

    private static final String ASSISTANT_FALLBACK_NOTE =
            "O assistente não pôde ser usado agora; o plano foi montado pelas regras.";

    private final TaskService taskService;
    private final CheckInService checkInService;
    private final AssistantService assistantService;
    private final DayPlanner planner;
    private final DayReplanner replanner;
    private final DayPlanRepository dayPlanRepository;
    private final TransactionTemplate transaction;

    public DayPlanService(TaskService taskService, CheckInService checkInService, AssistantService assistantService,
                          DayPlanner planner, DayReplanner replanner, DayPlanRepository dayPlanRepository,
                          TransactionTemplate transaction) {
        this.taskService = taskService;
        this.checkInService = checkInService;
        this.assistantService = assistantService;
        this.planner = planner;
        this.replanner = replanner;
        this.dayPlanRepository = dayPlanRepository;
        this.transaction = transaction;
    }

    public DayPlanResponseDTO plan(UUID userId, LocalDate day, LocalTime start, LocalTime end, boolean useAssistant) {
        Composed composed = compose(userId, day, start, end, useAssistant);
        Plan plan = composed.plan();
        return new DayPlanResponseDTO(
                day,
                composed.energy(),
                composed.source(),
                composed.note(),
                end,
                plan.blocks().stream().map(DayPlanResponseDTO.Block::from).toList(),
                plan.unscheduled().stream()
                        .map(t -> new DayPlanResponseDTO.Unscheduled(t.id(), t.title(), t.minutes()))
                        .toList(),
                List.of());
    }


    public DayPlanResponseDTO save(UUID userId, LocalDate day, SaveDayPlanRequestDTO request) {
        Composed composed = compose(userId, day, request.start(), request.end(), request.assistant());
        return transaction.execute(status -> {
            DayPlan saved = dayPlanRepository.findByUserIdAndPlanDate(userId, day)
                    .orElseGet(() -> new DayPlan(userId, day));
            saved.replace(composed.plan(), request.end(), composed.source(), composed.energy(), composed.note());
            return DayPlanResponseDTO.from(dayPlanRepository.save(saved), List.of());
        });
    }

    @Transactional(readOnly = true)
    public DayPlanResponseDTO saved(UUID userId, LocalDate day) {
        return DayPlanResponseDTO.from(findSaved(userId, day), List.of());
    }


    @Transactional
    public DayPlanResponseDTO replan(UUID userId, LocalDate day, LocalTime now) {
        DayPlan saved = findSaved(userId, day);
        List<DayPlanner.Block> previous = saved.blocksAsPlanned();
        List<UUID> previousTaskIds = previous.stream()
                .filter(b -> b.kind() == BlockKind.TASK)
                .map(DayPlanner.Block::taskId)
                .toList();
        Integer energy = checkInService.levelOn(userId, day).orElse(null);

        Replan replan = replanner.replan(previous, plannable(userId, day),
                taskService.doneAmong(userId, previousTaskIds), day, energy, now, saved.getWindowEnd());

        saved.replace(replan.plan(), saved.getWindowEnd(), saved.getSource(), energy, null);
        return DayPlanResponseDTO.from(saved, replan.changes());
    }

    private Composed compose(UUID userId, LocalDate day, LocalTime start, LocalTime end, boolean useAssistant) {
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horário de início precisa ser antes do fim.");
        }

        Integer energy = checkInService.levelOn(userId, day).orElse(null);
        List<PlannableTask> ordered = planner.order(plannable(userId, day), day, energy);

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
        return new Composed(planner.layout(ordered, start, end), energy, source, note);
    }

    private List<PlannableTask> plannable(UUID userId, LocalDate day) {
        return taskService.plannableFor(userId, day).stream()
                .map(t -> new PlannableTask(t.getId(), t.getTitle(), t.getPriority(), t.getEstimatedMinutes(), t.getDueDate()))
                .toList();
    }

    private DayPlan findSaved(UUID userId, LocalDate day) {
        return dayPlanRepository.findByUserIdAndPlanDate(userId, day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum plano salvo para este dia."));
    }

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

    private record Composed(Plan plan, Integer energy, Source source, String note) {
    }
}
