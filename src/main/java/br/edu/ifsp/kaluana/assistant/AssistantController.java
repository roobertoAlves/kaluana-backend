package br.edu.ifsp.kaluana.assistant;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
