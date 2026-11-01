package br.edu.ifsp.kaluana.reminder;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<ReminderResponseDTO> list(@AuthenticationPrincipal UUID userId) {
        return reminderService.list(userId);
    }

    @PostMapping
    public ResponseEntity<ReminderResponseDTO> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ReminderRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.create(userId, request));
    }

    @PutMapping("/{id}")
    public ReminderResponseDTO update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody ReminderRequestDTO request) {
        return reminderService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        reminderService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
