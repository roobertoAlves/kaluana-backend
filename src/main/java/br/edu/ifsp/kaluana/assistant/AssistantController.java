package br.edu.ifsp.kaluana.assistant;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/guide/rank")
    public RankResponse rankOptions(@AuthenticationPrincipal UUID userId, @Valid @RequestBody RankRequest request) {
        return new RankResponse(assistantService.rankOptions(userId, request.question(), request.options(), request.energyLevel()));
    }

    @PostMapping("/tasks/parse")
    public ParsedTaskDTO parseTask(@AuthenticationPrincipal UUID userId, @Valid @RequestBody ParseRequest request) {
        return assistantService.parseTask(userId, request.phrase(), request.today());
    }

    public record RankRequest(
            @NotBlank @Size(max = 300) String question,
            @NotNull @Size(min = 2, max = 3, message = "Cada etapa tem 2 ou 3 opções.") List<@NotBlank @Size(max = 120) String> options,
            @Min(1) @Max(5) Integer energyLevel) {
    }

    public record RankResponse(List<Integer> order) {
    }

    public record ParseRequest(
            @NotBlank(message = "Escreva a frase da tarefa.") @Size(max = 400) String phrase,
            @NotNull LocalDate today) {
    }
}
