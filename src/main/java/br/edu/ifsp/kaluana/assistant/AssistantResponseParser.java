package br.edu.ifsp.kaluana.assistant;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final class AssistantResponseParser {

    static final int MAX_STEPS = 8;
    static final int MAX_STEP_LENGTH = 120;

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
