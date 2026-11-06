package br.edu.ifsp.kaluana.review;

import br.edu.ifsp.kaluana.review.DayReviewRequestDTO.Move;
import br.edu.ifsp.kaluana.task.ReviewAction;
import br.edu.ifsp.kaluana.task.TaskService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DayReviewServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final LocalDate DAY = LocalDate.of(2026, 11, 2);

    @Mock
    private DayReviewRepository dayReviewRepository;

    @Mock
    private TaskService taskService;

    private DayReviewService service;

    @BeforeEach
    void setUp() {
        service = new DayReviewService(dayReviewRepository, taskService);
    }

    @Test
    void deveAplicarCadaDecisaoEGuardarSoAsContagens() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        when(dayReviewRepository.findByUserIdAndReviewDate(USER_ID, DAY)).thenReturn(Optional.empty());
        when(dayReviewRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DayReviewResponseDTO review = service.review(USER_ID, DAY, new DayReviewRequestDTO(DayFeeling.OK, List.of(
                new Move(a, ReviewAction.TOMORROW), new Move(b, ReviewAction.NO_DATE), new Move(c, ReviewAction.TOMORROW))));

        verify(taskService).applyReview(USER_ID, a, ReviewAction.TOMORROW, DAY);
        verify(taskService).applyReview(USER_ID, b, ReviewAction.NO_DATE, DAY);
        assertThat(review.movedTomorrow()).isEqualTo(2);
        assertThat(review.unscheduled()).isEqualTo(1);
        assertThat(review.completed()).isZero();
        assertThat(review.feeling()).isEqualTo(DayFeeling.OK);
    }

    @Test
    void refazerNoMesmoDiaSomaENaoApagaOComoFoi() {
        DayReview existing = new DayReview(USER_ID, DAY);
        existing.describe(DayFeeling.GOOD);
        existing.record(ReviewAction.DONE);
        when(dayReviewRepository.findByUserIdAndReviewDate(USER_ID, DAY)).thenReturn(Optional.of(existing));
        when(dayReviewRepository.save(existing)).thenReturn(existing);

        DayReviewResponseDTO review = service.review(USER_ID, DAY,
                new DayReviewRequestDTO(null, List.of(new Move(UUID.randomUUID(), ReviewAction.DONE))));

        assertThat(review.completed()).isEqualTo(2);
        assertThat(review.feeling()).isEqualTo(DayFeeling.GOOD);
    }

    @Test
    void tarefaDeOutraPessoaInterrompeARevisao() {
        UUID other = UUID.randomUUID();
        when(dayReviewRepository.findByUserIdAndReviewDate(USER_ID, DAY)).thenReturn(Optional.empty());
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada."))
                .when(taskService).applyReview(USER_ID, other, ReviewAction.TOMORROW, DAY);

        assertThatThrownBy(() -> service.review(USER_ID, DAY,
                new DayReviewRequestDTO(null, List.of(new Move(other, ReviewAction.TOMORROW)))))
                .isInstanceOf(ResponseStatusException.class);
        verify(dayReviewRepository, never()).save(any());
    }

    @Test
    void diaSemRevisaoResponde404() {
        when(dayReviewRepository.findByUserIdAndReviewDate(USER_ID, DAY)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(USER_ID, DAY))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void cadaTarefaSoPodeAparecerUmaVez() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        UUID same = UUID.randomUUID();
        DayReviewRequestDTO request = new DayReviewRequestDTO(null, List.of(new Move(same, ReviewAction.DONE), new Move(same, ReviewAction.TOMORROW)));

        assertThat(validator.validate(request)).extracting(v -> v.getMessage())
                .contains("Cada tarefa só pode aparecer uma vez na revisão.");
    }
}
