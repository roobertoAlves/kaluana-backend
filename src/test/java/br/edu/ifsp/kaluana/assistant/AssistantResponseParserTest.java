package br.edu.ifsp.kaluana.assistant;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssistantResponseParserTest {

    @Test
    void deveAceitarJsonComTextoEmVolta() {
        String content = "Claro! Aqui está:\n{\"ordem\": [2, 0, 1]}\nBom trabalho.";

        assertThat(AssistantResponseParser.parseOrder(content, 3)).containsExactly(2, 0, 1);
    }

    @Test
    void deveDescartarIndicesInvalidosERepetidos() {
        String content = "{\"ordem\": [1, 1, 7, -1, \"x\", 0]}";

        assertThat(AssistantResponseParser.parseOrder(content, 2)).containsExactly(1, 0);
    }

    @Test
    void deveFalharQuandoNaoHaJson() {
        assertThatThrownBy(() -> AssistantResponseParser.parseOrder("Não sei.", 3))
                .isInstanceOf(AssistantUnavailableException.class);
    }

    @Test
    void deveLimparELimitarAsEtapas() {
        String longa = "a".repeat(300);
        String content = "{\"etapas\": [\"  Separar\\n o material \", \"\", 42, \"" + longa + "\","
                + " \"3\", \"4\", \"5\", \"6\", \"7\", \"8\", \"9\"]}";

        List<String> steps = AssistantResponseParser.parseSteps(content);

        assertThat(steps).hasSize(AssistantResponseParser.MAX_STEPS);
        assertThat(steps.get(0)).isEqualTo("Separar o material");
        assertThat(steps.get(1)).hasSize(AssistantResponseParser.MAX_STEP_LENGTH);
    }
}
