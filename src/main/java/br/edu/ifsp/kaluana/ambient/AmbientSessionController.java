package br.edu.ifsp.kaluana.ambient;

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

/** RF16 · /api/ambient-sessions. O som toca no app; aqui só fica o registro. */
@RestController
@RequestMapping("/api/ambient-sessions")
public class AmbientSessionController {

    private final AmbientSessionService ambientSessionService;

    public AmbientSessionController(AmbientSessionService ambientSessionService) {
        this.ambientSessionService = ambientSessionService;
    }

    @GetMapping
    public List<AmbientSessionResponseDTO> list(@AuthenticationPrincipal UUID userId) {
        return ambientSessionService.list(userId);
    }

    @PostMapping
    public ResponseEntity<AmbientSessionResponseDTO> register(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AmbientSessionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ambientSessionService.register(userId, request));
    }
}
