package br.edu.ifsp.kaluana.assistant;

import java.util.List;

/** Usado quando app.assistant.enabled=false (padrão): o app funciona sem IA (RNF7). */
class DisabledPlanningAssistant implements PlanningAssistant {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String modelName() {
        return null;
    }

    @Override
    public List<Integer> suggestOrder(List<TaskSummary> tasks, Integer energyLevel) {
        throw new AssistantUnavailableException("Assistente desligado.");
    }

    @Override
    public List<String> suggestSteps(String taskTitle) {
        throw new AssistantUnavailableException("Assistente desligado.");
    }
}
