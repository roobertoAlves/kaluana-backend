package br.edu.ifsp.kaluana.task;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskTest {

    @Test
    void deveRegistrarEDesfazerConclusao() {
        Task task = new Task(UUID.randomUUID());

        task.markDone(true);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(task.getCompletedAt()).isNotNull();

        task.markDone(false);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getCompletedAt()).isNull();
    }

    @Test
    void deveNumerarMicroetapasEmOrdem() {
        Task task = new Task(UUID.randomUUID());

        task.addStep("Abrir o arquivo");
        task.addStep("Ler o enunciado");

        assertThat(task.getSteps()).extracting(TaskStep::getPosition).containsExactly(0, 1);
    }

    @Test
    void deveLimitarQuantidadeDeMicroetapas() {
        Task task = new Task(UUID.randomUUID());
        for (int i = 0; i < Task.MAX_STEPS; i++) {
            task.addStep("Etapa " + i);
        }

        assertThatThrownBy(() -> task.addStep("Mais uma"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("até 12");
    }

    @Test
    void deveResponder404AoMexerEmEtapaInexistente() {
        Task task = new Task(UUID.randomUUID());

        assertThatThrownBy(() -> task.removeStep(UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("não encontrada");
    }

    @Test
    void naoDeveRemarcarTarefaConcluida() {
        Task task = new Task(UUID.randomUUID());
        task.reschedule(java.time.LocalDate.of(2026, 11, 3));
        assertThat(task.getDueDate()).isEqualTo(java.time.LocalDate.of(2026, 11, 3));

        task.markDone(true);
        assertThatThrownBy(() -> task.reschedule(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("já foi concluída");
    }
}
