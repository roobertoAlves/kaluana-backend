package br.edu.ifsp.kaluana.task;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponseDTO> list(@AuthenticationPrincipal UUID userId, @RequestParam(required = false) Boolean done) {
        return taskService.list(userId, done);
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@AuthenticationPrincipal UUID userId, @Valid @RequestBody TaskRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(userId, request));
    }

    @PutMapping("/{id}")
    public TaskResponseDTO update(@AuthenticationPrincipal UUID userId, @PathVariable UUID id, @Valid @RequestBody TaskRequestDTO request) {
        return taskService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        taskService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/steps")
    public ResponseEntity<TaskResponseDTO> addStep(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody TaskStepRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.addStep(userId, id, request));
    }

    @PutMapping("/{id}/steps/{stepId}")
    public TaskResponseDTO updateStep(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID stepId,
            @Valid @RequestBody TaskStepRequestDTO request) {
        return taskService.updateStep(userId, id, stepId, request);
    }

    @DeleteMapping("/{id}/steps/{stepId}")
    public TaskResponseDTO removeStep(@AuthenticationPrincipal UUID userId, @PathVariable UUID id, @PathVariable UUID stepId) {
        return taskService.removeStep(userId, id, stepId);
    }
}
