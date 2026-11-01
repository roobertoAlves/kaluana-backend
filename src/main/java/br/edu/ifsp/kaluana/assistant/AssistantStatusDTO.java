package br.edu.ifsp.kaluana.assistant;

/** O app só mostra os botões do assistente quando {@code enabled} é true. */
public record AssistantStatusDTO(boolean enabled, String model) {
}
