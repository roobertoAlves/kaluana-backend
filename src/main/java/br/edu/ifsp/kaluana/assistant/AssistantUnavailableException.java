package br.edu.ifsp.kaluana.assistant;

/** O serviço de IA falhou, demorou demais ou respondeu fora do formato. */
public class AssistantUnavailableException extends RuntimeException {

    public AssistantUnavailableException(String message) {
        super(message);
    }

    public AssistantUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
