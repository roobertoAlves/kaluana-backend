package br.edu.ifsp.kaluana.task;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> list(UUID userId, Boolean done) {
        List<Task> tasks = done == null
                ? taskRepository.findTop300ByUserIdOrderByCreatedAtDesc(userId)
                : taskRepository.findTop300ByUserIdAndStatusOrderByCreatedAtDesc(userId, done ? TaskStatus.DONE : TaskStatus.TODO);
        return tasks.stream().map(TaskResponseDTO::from).toList();
    }

    @Transactional
    public TaskResponseDTO create(UUID userId, TaskRequestDTO request) {
        Task task = new Task(userId);
        describe(task, request);
        if (request.steps() != null) {
            request.steps().forEach(text -> task.addStep(text.trim()));
        }
        return TaskResponseDTO.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponseDTO update(UUID userId, UUID taskId, TaskRequestDTO request) {
        Task task = findOwned(userId, taskId);
        describe(task, request);
        return TaskResponseDTO.from(task);
    }

    @Transactional
    public void delete(UUID userId, UUID taskId) {
        taskRepository.delete(findOwned(userId, taskId));
    }

    @Transactional
    public TaskResponseDTO addStep(UUID userId, UUID taskId, TaskStepRequestDTO request) {
        Task task = findOwned(userId, taskId);
        task.addStep(request.text().trim());
        return TaskResponseDTO.from(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponseDTO updateStep(UUID userId, UUID taskId, UUID stepId, TaskStepRequestDTO request) {
        Task task = findOwned(userId, taskId);
        task.updateStep(stepId, request.text().trim(), request.done());
        return TaskResponseDTO.from(task);
    }

    @Transactional
    public TaskResponseDTO removeStep(UUID userId, UUID taskId, UUID stepId) {
        Task task = findOwned(userId, taskId);
        task.removeStep(stepId);
        return TaskResponseDTO.from(task);
    }

    @Transactional(readOnly = true)
    public List<Task> plannableFor(UUID userId, LocalDate day) {
        return taskRepository.findPlannable(userId, day);
    }

    @Transactional(readOnly = true)
    public Set<UUID> doneAmong(UUID userId, Collection<UUID> taskIds) {
        return taskIds.isEmpty() ? Set.of() : Set.copyOf(taskRepository.findDoneIds(userId, taskIds));
    }

    @Transactional(readOnly = true)
    public String titleOf(UUID userId, UUID taskId) {
        return findOwned(userId, taskId).getTitle();
    }

    @Transactional(readOnly = true)
    public void ensureOwned(UUID userId, UUID taskId) {
        if (!taskRepository.existsByIdAndUserId(taskId, userId)) {
            throw notFound();
        }
    }

    private void describe(Task task, TaskRequestDTO request) {
        task.describe(request.title().trim(), request.priority(), request.estimatedMinutes(), request.dueDate());
        if (request.done() != null) {
            task.markDone(request.done());
        }
    }

    private Task findOwned(UUID userId, UUID taskId) {
        return taskRepository.findByIdAndUserId(taskId, userId).orElseThrow(TaskService::notFound);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada.");
    }
}
