package br.edu.ifsp.kaluana.event;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class LifeEventController {

    private final LifeEventService lifeEventService;

    public LifeEventController(LifeEventService lifeEventService) {
        this.lifeEventService = lifeEventService;
    }

    @GetMapping
    public List<LifeEventResponseDTO> list(@AuthenticationPrincipal UUID userId) {
        return lifeEventService.list(userId);
    }

    @PostMapping
    public ResponseEntity<LifeEventResponseDTO> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody LifeEventRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lifeEventService.create(userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        lifeEventService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
