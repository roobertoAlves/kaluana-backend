package br.edu.ifsp.kaluana.assistant;

import java.time.LocalDate;
import java.util.List;

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

    @Override
    public List<Integer> rankOptions(String question, List<String> options, Integer energyLevel) {
        throw new AssistantUnavailableException("Assistente desligado.");
    }

    @Override
    public ParsedTaskDTO parseTask(String phrase, LocalDate today) {
        throw new AssistantUnavailableException("Assistente desligado.");
    }
}
