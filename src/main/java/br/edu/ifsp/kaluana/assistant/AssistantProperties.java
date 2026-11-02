package br.edu.ifsp.kaluana.assistant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * RF11 / RNF7: configuração do assistente de IA. Desligado por padrão. Funciona
 * com qualquer API compatível com "chat completions" da OpenAI, inclusive o
 * Ollama local (base-url http://localhost:11434/v1, sem chave), que é gratuito.
 */
@ConfigurationProperties(prefix = "app.assistant")
public record AssistantProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("http://localhost:11434/v1") String baseUrl,
        @DefaultValue("llama3.2:3b") String model,
        @DefaultValue("") String apiKey,
        @DefaultValue("20s") Duration timeout,
        @DefaultValue("20") int maxRequestsPerHour,
        // Modelos de "raciocínio" (ex.: qwen3.5) gastam os tokens pensando e não
        // respondem; "none" desliga isso no Ollama. Vazio = não enviar o campo.
        @DefaultValue("none") String reasoningEffort
) {
}
