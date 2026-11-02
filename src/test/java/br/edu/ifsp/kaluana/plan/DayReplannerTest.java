package br.edu.ifsp.kaluana.plan;

import br.edu.ifsp.kaluana.plan.DayPlanner.Block;
import br.edu.ifsp.kaluana.plan.DayPlanner.BlockKind;
import br.edu.ifsp.kaluana.plan.DayPlanner.PlannableTask;
import br.edu.ifsp.kaluana.plan.DayReplanner.Change;
import br.edu.ifsp.kaluana.plan.DayReplanner.ChangeKind;
import br.edu.ifsp.kaluana.plan.DayReplanner.Replan;
import br.edu.ifsp.kaluana.task.TaskPriority;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

class DayReplannerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);
    private static final LocalTime END = LocalTime.of(12, 0);

    private final DayPlanner planner = new DayPlanner();
    private final DayReplanner replanner = new DayReplanner(planner);

    // Plano aceito às 09:00: A 09:00–09:30, pausa, B 09:35–10:05, pausa, C 10:10–10:40.
    private final PlannableTask a = task("A", 30);
    private final PlannableTask b = task("B", 30);
    private final PlannableTask c = task("C", 30);
    private final List<Block> accepted = planner.layout(List.of(a, b, c), time("09:00"), END).blocks();

    @Test
    void tarefaAtrasadaEmpurraORestoSemMudarAOrdem() {
        // 09:42: A não foi concluída; B já começou mas está em andamento.
        Replan replan = replanner.replan(accepted, List.of(a, b, c), Set.of(), DAY, null, time("09:42"), END);

        assertThat(taskTitles(replan)).containsExactly("B", "A", "C");
        assertThat(replan.plan().blocks().get(0).start()).isEqualTo(time("09:35"));
        assertThat(replan.changes()).extracting(Change::title, Change::kind, Change::from, Change::to)
                .containsExactly(
                        tuple("A", ChangeKind.MOVED, time("09:00"), time("10:10")),
                        tuple("C", ChangeKind.MOVED, time("10:10"), time("10:45")));
    }

    @Test
    void tarefaConcluidaAntesDoHorarioAdiantaAsSeguintes() {
        // 09:12: A ficou pronta cedo.
        Replan replan = replanner.replan(accepted, List.of(b, c), Set.of(a.id()), DAY, null, time("09:12"), END);

        assertThat(taskTitles(replan)).containsExactly("B", "C");
        assertThat(replan.changes()).extracting(Change::title, Change::kind)
                .containsExactly(
                        tuple("A", ChangeKind.DONE),
                        tuple("B", ChangeKind.MOVED),
                        tuple("C", ChangeKind.MOVED));
        assertThat(replan.plan().blocks().get(0).start()).isEqualTo(time("09:15"));
    }

    @Test
    void tarefaApagadaApareceComoRemovidaENovaEntraNoFim() {
        PlannableTask nova = task("Nova", 15);

        Replan replan = replanner.replan(accepted, List.of(a, c, nova), Set.of(), DAY, null, time("09:10"), END);

        assertThat(taskTitles(replan)).containsExactly("A", "C", "Nova");
        assertThat(replan.changes()).extracting(Change::title, Change::kind)
                .containsExactly(
                        tuple("B", ChangeKind.REMOVED),
                        tuple("C", ChangeKind.MOVED),
                        tuple("Nova", ChangeKind.ADDED));
    }

    @Test
    void oQueNaoCabeMaisSaiDoDiaSemSumir() {
        // 11:20, nada feito: só cabe uma tarefa até 12:00.
        Replan replan = replanner.replan(accepted, List.of(a, b, c), Set.of(), DAY, null, time("11:20"), END);

        assertThat(taskTitles(replan)).containsExactly("A");
        assertThat(replan.plan().unscheduled()).extracting(PlannableTask::title).containsExactly("B", "C");
        assertThat(replan.changes()).extracting(Change::kind)
                .containsExactly(ChangeKind.MOVED, ChangeKind.DROPPED, ChangeKind.DROPPED);
    }

    @Test
    void semMudancasNaoHaNadaAAvisar() {
        Replan replan = replanner.replan(accepted, List.of(a, b, c), Set.of(), DAY, null, time("09:20"), END);

        assertThat(replan.changes()).isEmpty();
        assertThat(replan.plan().blocks()).isEqualTo(accepted);
    }

    @Test
    void blocoJaFeitoNoPassadoNaoViraAviso() {
        Replan replan = replanner.replan(accepted, List.of(b, c), Set.of(a.id()), DAY, null, time("09:50"), END);

        assertThat(replan.changes()).extracting(Change::kind).doesNotContain(ChangeKind.DONE);
    }

    @Test
    void proximoHorarioArredondaParaCincoMinutos() {
        assertThat(DayReplanner.nextSlot(time("09:07"))).isEqualTo(time("09:10"));
        assertThat(DayReplanner.nextSlot(time("09:10"))).isEqualTo(time("09:10"));
        assertThat(DayReplanner.nextSlot(LocalTime.of(23, 58))).isEqualTo(time("23:55"));
    }

    private static List<String> taskTitles(Replan replan) {
        return replan.plan().blocks().stream()
                .filter(block -> block.kind() == BlockKind.TASK)
                .map(Block::title)
                .toList();
    }

    private static PlannableTask task(String title, int minutes) {
        return new PlannableTask(UUID.randomUUID(), title, TaskPriority.MEDIUM, minutes, null);
    }

    private static LocalTime time(String hhmm) {
        return LocalTime.parse(hhmm);
    }
}
