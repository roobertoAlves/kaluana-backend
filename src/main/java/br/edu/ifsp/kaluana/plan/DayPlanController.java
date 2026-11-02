package br.edu.ifsp.kaluana.plan;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * RF10 · GET /api/day-plan. A data e os horários vêm do aparelho porque o
 * servidor não sabe o fuso do usuário (ex.: start = agora, no horário local).
 */
@RestController
@RequestMapping("/api/day-plan")
public class DayPlanController {

    private final DayPlanService dayPlanService;

    public DayPlanController(DayPlanService dayPlanService) {
        this.dayPlanService = dayPlanService;
    }

    @GetMapping
    public DayPlanResponseDTO plan(
            @AuthenticationPrincipal UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "08:00") @DateTimeFormat(pattern = "HH:mm") LocalTime start,
            @RequestParam(defaultValue = "22:00") @DateTimeFormat(pattern = "HH:mm") LocalTime end,
            @RequestParam(defaultValue = "false") boolean assistant) {
        return dayPlanService.plan(userId, date, start, end, assistant);
    }
}
