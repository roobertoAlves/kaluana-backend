package br.edu.ifsp.kaluana.review;

import java.time.LocalDate;

public record DayReviewResponseDTO(
        LocalDate date,
        DayFeeling feeling,
        int movedTomorrow,
        int unscheduled,
        int completed
) {
    public static DayReviewResponseDTO from(DayReview review) {
        return new DayReviewResponseDTO(
                review.getReviewDate(),
                review.getFeeling(),
                review.getMovedTomorrow(),
                review.getUnscheduled(),
                review.getCompleted());
    }
}
