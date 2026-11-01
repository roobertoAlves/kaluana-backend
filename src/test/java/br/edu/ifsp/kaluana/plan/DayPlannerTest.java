package br.edu.ifsp.kaluana.plan;

import br.edu.ifsp.kaluana.plan.DayPlanner.BlockKind;
import br.edu.ifsp.kaluana.plan.DayPlanner.Plan;
import br.edu.ifsp.kaluana.plan.DayPlanner.PlannableTask;
import br.edu.ifsp.kaluana.task.TaskPriority;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DayPlannerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    private final DayPlanner planner = new DayPlanner();

    @Test
    void deveColocarAtrasadasEDoDiaAntesDasDemais() {
        PlannableTask semData = task("Sem data", TaskPriority.HIGH, 30, null);
        PlannableTask doDia = task("Do dia", TaskPriority.LOW, 30, DAY);
        PlannableTask atrasada = task("Atrasada", TaskPriority.LOW, 30, DAY.minusDays(2));

        List<PlannableTask> ordered = planner.order(List.of(semData, doDia, atrasada), DAY, null);

        assertThat(ordered).containsExactly(atrasada, doDia, semData);
    }

    @Test
    void comEnergiaBaixaDeveComecarPelasTarefasCurtas() {
        PlannableTask longa = task("Longa", TaskPriority.HIGH, 90, null);
        PlannableTask curta = task("Curta", TaskPriority.LOW, 10, null);

        assertThat(planner.order(List.of(longa, curta), DAY, 1)).containsExactly(curta, longa);
    }

    @Test
    void comEnergiaAltaDevePriorizarImportantesELongas() {
        PlannableTask curtaAlta = task("Curta alta", TaskPriority.HIGH, 10, null);
        PlannableTask longaAlta = task("Longa alta", TaskPriority.HIGH, 90, null);
        PlannableTask baixa = task("Baixa", TaskPriority.LOW, 60, null);

        assertThat(planner.order(List.of(baixa, curtaAlta, longaAlta), DAY, 5))
                .containsExactly(longaAlta, curtaAlta, baixa);
    }

    @Test
    void deveSerDeterministicoParaAMesmaEntrada() {
        List<PlannableTask> tasks = List.of(
                task("B", TaskPriority.MEDIUM, 25, null),
                task("A", TaskPriority.MEDIUM, 25, null),
                task("C", TaskPriority.MEDIUM, 25, null));

        assertThat(planner.order(tasks, DAY, 3)).isEqualTo(planner.order(List.copyOf(tasks).reversed(), DAY, 3));
    }

    @Test
    void deveInserirPausasEUsarDuracaoPadrao() {
        PlannableTask primeira = task("Primeira", TaskPriority.MEDIUM, 30, null);
        PlannableTask semDuracao = task("Sem duração", TaskPriority.MEDIUM, null, null);

        Plan plan = planner.layout(List.of(primeira, semDuracao), LocalTime.of(9, 0), LocalTime.of(12, 0));

        assertThat(plan.blocks()).extracting(DayPlanner.Block::kind)
                .containsExactly(BlockKind.TASK, BlockKind.BREAK, BlockKind.TASK);
        assertThat(plan.blocks().get(2).start()).isEqualTo(LocalTime.of(9, 35));
        assertThat(plan.blocks().get(2).end()).isEqualTo(LocalTime.of(10, 0));
        assertThat(plan.unscheduled()).isEmpty();
    }

    @Test
    void deveSepararOQueNaoCabeSemDescartarTarefasMenores() {
        PlannableTask grande = task("Grande", TaskPriority.HIGH, 120, null);
        PlannableTask pequena = task("Pequena", TaskPriority.LOW, 20, null);

        Plan plan = planner.layout(List.of(grande, pequena), LocalTime.of(21, 0), LocalTime.of(22, 0));

        assertThat(plan.unscheduled()).containsExactly(grande);
        assertThat(plan.blocks()).singleElement().satisfies(block -> assertThat(block.title()).isEqualTo("Pequena"));
    }

    private static PlannableTask task(String title, TaskPriority priority, Integer minutes, LocalDate due) {
        return new PlannableTask(UUID.nameUUIDFromBytes(title.getBytes()), title, priority, minutes, due);
    }
}
