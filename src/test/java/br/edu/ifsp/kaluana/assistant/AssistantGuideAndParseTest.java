package br.edu.ifsp.kaluana.assistant;

import br.edu.ifsp.kaluana.security.RedisRateLimiter;
import br.edu.ifsp.kaluana.task.TaskPriority;
import br.edu.ifsp.kaluana.task.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssistantGuideAndParseTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 8);

    @Mock
    private RedisRateLimiter rateLimiter;

    @Mock
    private TaskService taskService;

    @Test
    void ordemDoGuiaCompletaAsOpcoesQueFaltam() {
        assertThat(AssistantResponseParser.parseRanking("{\"ordem\": [2]}", 3)).containsExactly(2, 0, 1);
    }

    @Test
    void ordemDoGuiaIgnoraIndicesInventados() {
        assertThat(AssistantResponseParser.parseRanking("{\"ordem\": [5, 1, 1]}", 2)).containsExactly(1, 0);
    }

    @Test
    void fraseViraTarefaComCamposValidados() {
        String resposta = "{\"titulo\": \"Estudar cálculo\", \"data\": \"2026-11-09\", \"minutos\": 30,"
                + " \"prioridade\": \"high\", \"etapas\": [\"Abrir o livro\", \"\", 3]}";

        ParsedTaskDTO task = AssistantResponseParser.parseTask(resposta, HOJE);

        assertThat(task.title()).isEqualTo("Estudar cálculo");
        assertThat(task.dueDate()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(task.estimatedMinutes()).isEqualTo(30);
        assertThat(task.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(task.steps()).containsExactly("Abrir o livro");
    }

    @Test
    void camposForaDasRegrasSaoDescartados() {
        String resposta = "{\"titulo\": \"Ligar\", \"data\": \"2020-01-01\", \"minutos\": 2, \"prioridade\": \"URGENTE\"}";

        ParsedTaskDTO task = AssistantResponseParser.parseTask(resposta, HOJE);

        assertThat(task.dueDate()).isNull();
        assertThat(task.estimatedMinutes()).isNull();
        assertThat(task.priority()).isNull();
        assertThat(task.steps()).isEmpty();
    }

    @Test
    void fraseSemTituloEhRecusada() {
        assertThatThrownBy(() -> AssistantResponseParser.parseTask("{\"titulo\": \"  \"}", HOJE))
                .isInstanceOf(AssistantUnavailableException.class);
    }

    @Test
    void assistenteDesligadoMantemAOrdemPadraoSemGastarCota() {
        AssistantService service = new AssistantService(new DisabledPlanningAssistant(),
                new AssistantProperties(false, "http://localhost", "modelo", "", Duration.ofSeconds(5), 20, ""),
                rateLimiter, taskService);

        assertThat(service.rankOptions(UUID.randomUUID(), "O que fazer agora?", List.of("Pausar", "Respirar", "Pedir ajuda"), 2))
                .containsExactly(0, 1, 2);
        verify(rateLimiter, never()).tryConsume(anyString(), org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.any());
    }
}
