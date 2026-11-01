package br.edu.ifsp.kaluana.plan;

import br.edu.ifsp.kaluana.plan.DayPlanner.BlockKind;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Plano do dia (RF10). {@code source} diz se a ordem veio das regras ou do
 * assistente de IA; {@code note} explica quando o assistente foi pedido e não
 * pôde ser usado.
 */
public record DayPlanResponseDTO(
        LocalDate date,
        Integer energyLevel,
        Source source,
        String note,
        List<Block> blocks,
        List<Unscheduled> unscheduled
) {

    public enum Source { RULES, ASSISTANT }

    public record Block(
            BlockKind kind,
            UUID taskId,
            String title,
            @JsonFormat(pattern = "HH:mm") LocalTime start,
            @JsonFormat(pattern = "HH:mm") LocalTime end) {
    }

    public record Unscheduled(UUID taskId, String title, int minutes) {
    }
}
