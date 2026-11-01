package br.edu.ifsp.kaluana.assistant;

import java.util.List;

/**
 * Porta para o serviço de IA (RF11). Recebe só o mínimo (título e duração das
 * tarefas, RNF7) e devolve respostas já validadas. Falhas viram
 * {@link AssistantUnavailableException}; quem chama decide o plano B.
 */
public interface PlanningAssistant {

    record TaskSummary(String title, int minutes) {
    }

    boolean isEnabled();

    /** Nome do modelo, para exibir na tela ("assistente: llama3.2:3b"). */
    String modelName();

    /** Índices de {@code tasks} na ordem sugerida, sem repetição e só índices válidos. */
    List<Integer> suggestOrder(List<TaskSummary> tasks, Integer energyLevel);

    /** Microetapas sugeridas para uma tarefa (no máximo 8, curtas). */
    List<String> suggestSteps(String taskTitle);
}
