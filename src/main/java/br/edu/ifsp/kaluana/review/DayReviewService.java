package br.edu.ifsp.kaluana.review;

import br.edu.ifsp.kaluana.task.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.UUID;


@Service
public class DayReviewService {

    private final DayReviewRepository dayReviewRepository;
    private final TaskService taskService;

    public DayReviewService(DayReviewRepository dayReviewRepository, TaskService taskService) {
        this.dayReviewRepository = dayReviewRepository;
        this.taskService = taskService;
    }

    @Transactional
    public DayReviewResponseDTO review(UUID userId, LocalDate day, DayReviewRequestDTO request) {
        DayReview review = dayReviewRepository.findByUserIdAndReviewDate(userId, day)
                .orElseGet(() -> new DayReview(userId, day));
        for (DayReviewRequestDTO.Move move : request.moves()) {
            taskService.applyReview(userId, move.taskId(), move.action(), day);
            review.record(move.action());
        }
        review.describe(request.feeling());
        return DayReviewResponseDTO.from(dayReviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public DayReviewResponseDTO find(UUID userId, LocalDate day) {
        return dayReviewRepository.findByUserIdAndReviewDate(userId, day)
                .map(DayReviewResponseDTO::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "O dia ainda não foi revisado."));
    }
}
