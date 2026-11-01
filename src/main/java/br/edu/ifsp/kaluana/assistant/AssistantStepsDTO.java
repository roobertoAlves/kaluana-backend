package br.edu.ifsp.kaluana.assistant;

import java.util.List;

/** Sugestão de microetapas. Não é salva: o usuário escolhe quais adicionar à tarefa. */
public record AssistantStepsDTO(List<String> steps) {
}
