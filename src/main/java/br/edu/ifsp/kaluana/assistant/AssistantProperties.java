package br.edu.ifsp.kaluana.assistant;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;


@ConfigurationProperties(prefix = "app.assistant")
public record AssistantProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("http://localhost:11434/v1") String baseUrl,
        @DefaultValue("llama3.2:3b") String model,
        @DefaultValue("") String apiKey,
        @DefaultValue("20s") Duration timeout,
        @DefaultValue("20") int maxRequestsPerHour,

        @DefaultValue("none") String reasoningEffort
) {
}
