package br.edu.ifsp.kaluana.assistant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AssistantConfig {

    @Bean
    PlanningAssistant planningAssistant(AssistantProperties properties) {
        return properties.enabled()
                ? new OpenAiCompatiblePlanningAssistant(properties)
                : new DisabledPlanningAssistant();
    }
}
