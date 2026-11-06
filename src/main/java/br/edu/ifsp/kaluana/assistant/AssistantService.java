package br.edu.ifsp.kaluana.assistant;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.edu.ifsp.kaluana.assistant.PlanningAssistant.TaskSummary;
import br.edu.ifsp.kaluana.security.RedisRateLimiter;
import br.edu.ifsp.kaluana.task.TaskService;


@Service
public class AssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantService.class);
    private static final Duration LIMIT_WINDOW = Duration.ofHours(1);

    private final PlanningAssistant assistant;
    private final AssistantProperties properties;
    private final RedisRateLimiter rateLimiter;
    private final TaskService taskService;

    public AssistantService(PlanningAssistant assistant, AssistantProperties properties,
                            RedisRateLimiter rateLimiter, TaskService taskService) {
        this.assistant = assistant;
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        this.taskService = taskService;
    }

    public AssistantStatusDTO status() {
        return new AssistantStatusDTO(assistant.isEnabled(), assistant.modelName());
    }

    public Optional<List<Integer>> suggestOrder(UUID userId, List<TaskSummary> tasks, Integer energyLevel) {
        if (!assistant.isEnabled() || tasks.isEmpty()) {
            return Optional.empty();
        }
        consumeQuota(userId);
        try {
            return Optional.of(assistant.suggestOrder(tasks, energyLevel));
        } catch (AssistantUnavailableException e) {
            logger.warn("Assistente indisponível ao ordenar tarefas: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public AssistantStepsDTO suggestSteps(UUID userId, UUID taskId) {
        if (!assistant.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "O assistente está desligado.");
        }
        String title = taskService.titleOf(userId, taskId);
        consumeQuota(userId);
        try {
            return new AssistantStepsDTO(assistant.suggestSteps(title));
        } catch (AssistantUnavailableException e) {
            logger.warn("Assistente indisponível ao sugerir etapas: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "O assistente não respondeu agora. Tente de novo em instantes.");
        }
    }

    private void consumeQuota(UUID userId) {
        if (!rateLimiter.tryConsume("assistant:" + userId, properties.maxRequestsPerHour(), LIMIT_WINDOW)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Você usou o assistente muitas vezes nesta hora. O plano pelas regras continua disponível.");
        }
    }
}
