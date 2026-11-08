package br.edu.ifsp.kaluana.assistant;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifsp.kaluana.task.TaskPriority;

final class AssistantResponseParser {

    static final int MAX_STEPS = 8;
    static final int MAX_STEP_LENGTH = 120;
    static final int MAX_TASK_STEPS = 12;
    static final int MAX_TITLE_LENGTH = 120;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AssistantResponseParser() {
    }

    static List<Integer> parseOrder(String content, int taskCount) {
        JsonNode order = firstJsonObject(content).path("ordem");
        if (!order.isArray()) {
            throw new AssistantUnavailableException("Resposta sem o campo 'ordem'.");
        }
        Set<Integer> indexes = new LinkedHashSet<>();
        order.forEach(node -> {
            if (node.canConvertToInt() && node.asInt() >= 0 && node.asInt() < taskCount) {
                indexes.add(node.asInt());
            }
        });
        if (indexes.isEmpty()) {
            throw new AssistantUnavailableException("Resposta sem índices válidos.");
        }
        return List.copyOf(indexes);
    }

    static List<String> parseSteps(String content) {
        JsonNode stepsNode = firstJsonObject(content).path("etapas");
        if (!stepsNode.isArray()) {
            throw new AssistantUnavailableException("Resposta sem o campo 'etapas'.");
        }
        List<String> steps = new ArrayList<>();
        for (JsonNode node : stepsNode) {
            if (steps.size() == MAX_STEPS) break;
            if (!node.isTextual()) continue;
            String text = node.asText().replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
            if (text.isEmpty()) continue;
            steps.add(text.length() > MAX_STEP_LENGTH ? text.substring(0, MAX_STEP_LENGTH).trim() : text);
        }
        if (steps.isEmpty()) {
            throw new AssistantUnavailableException("Resposta sem etapas válidas.");
        }
        return steps;
    }

    static List<Integer> parseRanking(String content, int optionCount) {
        List<Integer> order = new ArrayList<>(parseOrder(content, optionCount));
        for (int i = 0; i < optionCount; i++) {
            if (!order.contains(i)) {
                order.add(i);
            }
        }
        return List.copyOf(order);
    }

    static ParsedTaskDTO parseTask(String content, LocalDate today) {
        JsonNode node = firstJsonObject(content);
        String title = clean(node.path("titulo").asText(""), MAX_TITLE_LENGTH);
        if (title.isEmpty()) {
            throw new AssistantUnavailableException("Resposta sem título.");
        }
        LocalDate dueDate = null;
        if (node.path("data").isTextual()) {
            try {
                LocalDate parsed = LocalDate.parse(node.path("data").asText());
                if (!parsed.isBefore(today) && !parsed.isAfter(today.plusYears(1))) {
                    dueDate = parsed;
                }
            } catch (DateTimeParseException ignored) {
                dueDate = null;
            }
        }
        Integer minutes = null;
        if (node.path("minutos").canConvertToInt()) {
            int value = node.path("minutos").asInt();
            if (value >= 5 && value <= 480) {
                minutes = value;
            }
        }
        TaskPriority priority = null;
        String rawPriority = node.path("prioridade").asText("").trim().toUpperCase();
        for (TaskPriority p : TaskPriority.values()) {
            if (p.name().equals(rawPriority)) {
                priority = p;
            }
        }
        List<String> steps = new ArrayList<>();
        for (JsonNode step : node.path("etapas")) {
            if (steps.size() == MAX_TASK_STEPS) break;
            if (!step.isTextual()) continue;
            String text = clean(step.asText(), MAX_STEP_LENGTH);
            if (!text.isEmpty()) steps.add(text);
        }
        return new ParsedTaskDTO(title, dueDate, minutes, priority, List.copyOf(steps));
    }

    private static String clean(String text, int max) {
        String cleaned = text.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
        return cleaned.length() > max ? cleaned.substring(0, max).trim() : cleaned;
    }

    private static JsonNode firstJsonObject(String content) {
        if (content == null) {
            throw new AssistantUnavailableException("Resposta vazia.");
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new AssistantUnavailableException("Resposta sem JSON.");
        }
        try {
            return MAPPER.readTree(content.substring(start, end + 1));
        } catch (Exception e) {
            throw new AssistantUnavailableException("JSON inválido na resposta.", e);
        }
    }
}
