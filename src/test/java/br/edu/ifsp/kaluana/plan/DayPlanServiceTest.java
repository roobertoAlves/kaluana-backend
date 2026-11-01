package br.edu.ifsp.kaluana.plan;

import br.edu.ifsp.kaluana.assistant.AssistantService;
import br.edu.ifsp.kaluana.checkin.CheckInService;
import br.edu.ifsp.kaluana.plan.DayPlanResponseDTO.Source;
import br.edu.ifsp.kaluana.task.Task;
import br.edu.ifsp.kaluana.task.TaskPriority;
import br.edu.ifsp.kaluana.task.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DayPlanServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    @Mock
    private TaskService taskService;

    @Mock
    private CheckInService checkInService;

    @Mock
    private AssistantService assistantService;

    private DayPlanService service;

    @BeforeEach
    void setUp() {
        service = new DayPlanService(taskService, checkInService, assistantService, new DayPlanner());
    }

    @Test
    void deveUsarAOrdemDoAssistenteSemPerderTarefasOmitidas() {
        when(taskService.plannableFor(USER_ID, DAY)).thenReturn(List.of(task("A", 10), task("B", 20), task("C", 30)));
        when(checkInService.levelOn(USER_ID, DAY)).thenReturn(Optional.of(3));
        // Regras: A, B, C (mesma prioridade, mais curta antes). Assistente sugere só C.
        when(assistantService.suggestOrder(eq(USER_ID), anyList(), eq(3))).thenReturn(Optional.of(List.of(2)));

        DayPlanResponseDTO plan = service.plan(USER_ID, DAY, LocalTime.of(9, 0), LocalTime.of(18, 0), true);

        assertThat(plan.source()).isEqualTo(Source.ASSISTANT);
        assertThat(plan.blocks()).filteredOn(b -> b.kind() == DayPlanner.BlockKind.TASK)
                .extracting(DayPlanResponseDTO.Block::title)
                .containsExactly("C", "A", "B");
    }

    @Test
    void deveVoltarParaAsRegrasQuandoOAssistenteFalha() {
        when(taskService.plannableFor(USER_ID, DAY)).thenReturn(List.of(task("A", 10)));
        when(checkInService.levelOn(USER_ID, DAY)).thenReturn(Optional.empty());
        when(assistantService.suggestOrder(any(), anyList(), any())).thenReturn(Optional.empty());

        DayPlanResponseDTO plan = service.plan(USER_ID, DAY, LocalTime.of(9, 0), LocalTime.of(18, 0), true);

        assertThat(plan.source()).isEqualTo(Source.RULES);
        assertThat(plan.note()).contains("regras");
        assertThat(plan.blocks()).hasSize(1);
    }

    @Test
    void deveRecusarJanelaInvertida() {
        assertThatThrownBy(() -> service.plan(USER_ID, DAY, LocalTime.of(18, 0), LocalTime.of(9, 0), false))
                .isInstanceOf(ResponseStatusException.class);
    }

    private static Task task(String title, int minutes) {
        Task task = new Task(USER_ID);
        task.describe(title, TaskPriority.MEDIUM, minutes, null);
        return task;
    }
}
