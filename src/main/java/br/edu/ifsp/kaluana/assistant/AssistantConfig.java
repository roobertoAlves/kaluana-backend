package br.edu.ifsp.kaluana.assistant;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AssistantProperties.class)
class AssistantConfig {

    @Bean
    PlanningAssistant planningAssistant(AssistantProperties properties) {
        return properties.enabled()
                ? new OpenAiCompatiblePlanningAssistant(properties)
                : new DisabledPlanningAssistant();
    }
}
