package br.edu.ifsp.kaluana.plan;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * RF10 / RF14 · /api/day-plan. A data e os horários vêm do aparelho porque o
 * servidor não sabe o fuso do usuário (ex.: start = agora, no horário local).
 */
@RestController
@RequestMapping("/api/day-plan")
public class DayPlanController {

    private final DayPlanService dayPlanService;

    public DayPlanController(DayPlanService dayPlanService) {
        this.dayPlanService = dayPlanService;
    }

    /** Pré-visualização: monta o plano sem salvar. */
    @GetMapping
    public DayPlanResponseDTO plan(
            @AuthenticationPrincipal UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "08:00") @DateTimeFormat(pattern = "HH:mm") LocalTime start,
            @RequestParam(defaultValue = "22:00") @DateTimeFormat(pattern = "HH:mm") LocalTime end,
            @RequestParam(defaultValue = "false") boolean assistant) {
        return dayPlanService.plan(userId, date, start, end, assistant);
    }

    @GetMapping("/{date}")
    public DayPlanResponseDTO saved(
            @AuthenticationPrincipal UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return dayPlanService.saved(userId, date);
    }

    @PutMapping("/{date}")
    public DayPlanResponseDTO save(
            @AuthenticationPrincipal UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody SaveDayPlanRequestDTO request) {
        return dayPlanService.save(userId, date, request);
    }

    @PostMapping("/{date}/replan")
    public DayPlanResponseDTO replan(
            @AuthenticationPrincipal UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody ReplanRequestDTO request) {
        return dayPlanService.replan(userId, date, request.now());
    }
}
