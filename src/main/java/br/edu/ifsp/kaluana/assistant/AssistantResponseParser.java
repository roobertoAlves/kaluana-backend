package br.edu.ifsp.kaluana.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Valida o texto devolvido pelo modelo antes de qualquer uso (RNF7). O modelo
 * pode responder com texto em volta do JSON, índices inválidos ou repetidos e
 * etapas enormes: tudo isso é filtrado aqui. Função pura, testada isoladamente.
 */
final class AssistantResponseParser {

    static final int MAX_STEPS = 8;
    static final int MAX_STEP_LENGTH = 120;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AssistantResponseParser() {
    }

    /** Espera {"ordem": [2, 0, 1]}. Devolve índices válidos, distintos, na ordem dada. */
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

    /** Espera {"etapas": ["...", "..."]}. Remove vazias, caracteres de controle e excesso. */
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

    /** Pega o primeiro objeto JSON do texto (modelos às vezes escrevem algo antes ou depois). */
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
