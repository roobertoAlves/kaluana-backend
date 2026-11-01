package br.edu.ifsp.kaluana.assistant;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** RF11 · /api/assistant. A ordenação do dia fica em GET /api/day-plan?assistant=true. */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/status")
    public AssistantStatusDTO status() {
        return assistantService.status();
    }

    @PostMapping("/tasks/{taskId}/steps")
    public AssistantStepsDTO suggestSteps(@AuthenticationPrincipal UUID userId, @PathVariable UUID taskId) {
        return assistantService.suggestSteps(userId, taskId);
    }
}
