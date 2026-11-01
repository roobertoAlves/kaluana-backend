package br.edu.ifsp.kaluana.timer;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/timer-sessions")
public class TimerSessionController {

    private final TimerSessionService timerSessionService;

    public TimerSessionController(TimerSessionService timerSessionService) {
        this.timerSessionService = timerSessionService;
    }

    @GetMapping
    public List<TimerSessionResponseDTO> list(@AuthenticationPrincipal UUID userId) {
        return timerSessionService.list(userId);
    }

    @PostMapping
    public ResponseEntity<TimerSessionResponseDTO> register(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody TimerSessionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timerSessionService.register(userId, request));
    }
}
