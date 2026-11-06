package br.edu.ifsp.kaluana.timer;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.ifsp.kaluana.task.TaskService;

@Service
public class TimerSessionService {

    private final TimerSessionRepository timerSessionRepository;
    private final TaskService taskService;

    public TimerSessionService(TimerSessionRepository timerSessionRepository, TaskService taskService) {
        this.timerSessionRepository = timerSessionRepository;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public List<TimerSessionResponseDTO> list(UUID userId) {
        return timerSessionRepository.findTop200ByUserIdOrderByFinishedAtDesc(userId).stream()
                .map(TimerSessionResponseDTO::from)
                .toList();
    }

    @Transactional
    public TimerSessionResponseDTO register(UUID userId, TimerSessionRequestDTO request) {
        if (request.taskId() != null) {
            taskService.ensureOwned(userId, request.taskId());
        }

        TimerSession session = new TimerSession(
                userId,
                request.taskId(),
                request.task().trim(),
                request.plannedMinutes(),
                request.stepsCount(),
                request.water(),
                request.soundOrDefault(),
                request.status(),
                request.startedAt(),
                request.finishedAt());
        return TimerSessionResponseDTO.from(timerSessionRepository.save(session));
    }
}
