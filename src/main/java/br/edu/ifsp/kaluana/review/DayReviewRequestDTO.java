package br.edu.ifsp.kaluana.review;

import br.edu.ifsp.kaluana.task.ReviewAction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** PUT /api/day-reviews/{date}: decisões sobre as pendentes do dia e, opcionalmente, como foi o dia. */
public record DayReviewRequestDTO(

        // Opcional (RNF6): ausente = não informado.
        DayFeeling feeling,

        @NotNull(message = "Informe as decisões (a lista pode ser vazia).")
        @Size(max = 50, message = "A revisão aceita até 50 tarefas.")
        List<@Valid @NotNull Move> moves
) {

    public record Move(
            @NotNull(message = "Informe a tarefa.") UUID taskId,
            @NotNull(message = "Informe a ação (TOMORROW, NO_DATE ou DONE).") ReviewAction action) {
    }

    @AssertTrue(message = "Cada tarefa só pode aparecer uma vez na revisão.")
    public boolean isEachTaskOnce() {
        if (moves == null) return true;
        List<UUID> ids = moves.stream().filter(Objects::nonNull).map(Move::taskId).filter(Objects::nonNull).toList();
        return ids.size() == ids.stream().distinct().count();
    }
}
