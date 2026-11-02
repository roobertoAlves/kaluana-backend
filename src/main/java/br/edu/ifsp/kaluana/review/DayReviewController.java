package br.edu.ifsp.kaluana.review;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/** RF17 · /api/day-reviews/{date}. A data é a do aparelho (o servidor não sabe o fuso). */
@RestController
@RequestMapping("/api/day-reviews")
public class DayReviewController {

    private final DayReviewService dayReviewService;

    public DayReviewController(DayReviewService dayReviewService) {
        this.dayReviewService = dayReviewService;
    }

    @GetMapping("/{date}")
    public DayReviewResponseDTO find(
            @AuthenticationPrincipal UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dayReviewService.find(userId, date);
    }

    @PutMapping("/{date}")
    public DayReviewResponseDTO review(
            @AuthenticationPrincipal UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody DayReviewRequestDTO request) {
        return dayReviewService.review(userId, date, request);
    }
}
