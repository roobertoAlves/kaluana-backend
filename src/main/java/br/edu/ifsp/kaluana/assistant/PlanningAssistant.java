package br.edu.ifsp.kaluana.assistant;

import java.util.List;

public interface PlanningAssistant {

    record TaskSummary(String title, int minutes) {
    }

    boolean isEnabled();

    String modelName();

    List<Integer> suggestOrder(List<TaskSummary> tasks, Integer energyLevel);

    List<String> suggestSteps(String taskTitle);
}
