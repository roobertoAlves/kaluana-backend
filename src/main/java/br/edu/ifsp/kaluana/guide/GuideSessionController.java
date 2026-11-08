package br.edu.ifsp.kaluana.guide;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/guide-sessions")
@PreAuthorize("hasRole('PRIMARY')")
public class GuideSessionController {

    private final GuideSessionService guideSessionService;

    public GuideSessionController(GuideSessionService guideSessionService) {
        this.guideSessionService = guideSessionService;
    }

    @PostMapping
    public ResponseEntity<GuideSessionResponseDTO> record(@AuthenticationPrincipal UUID userId, @Valid @RequestBody GuideSessionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guideSessionService.record(userId, request));
    }

    @GetMapping
    public List<GuideSessionResponseDTO> list(@AuthenticationPrincipal UUID userId) {
        return guideSessionService.recent(userId);
    }
}
