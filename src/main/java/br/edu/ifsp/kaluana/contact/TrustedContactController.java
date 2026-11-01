package br.edu.ifsp.kaluana.contact;

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
@RequestMapping("/api/contacts")
public class TrustedContactController {

    private final TrustedContactService trustedContactService;

    public TrustedContactController(TrustedContactService trustedContactService) {
        this.trustedContactService = trustedContactService;
    }

    @GetMapping
    public List<TrustedContactResponseDTO> list(@AuthenticationPrincipal UUID userId) {
        return trustedContactService.list(userId);
    }

    @PostMapping
    public ResponseEntity<TrustedContactResponseDTO> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody TrustedContactRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trustedContactService.create(userId, request));
    }

    @PutMapping("/{id}")
    public TrustedContactResponseDTO update(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody TrustedContactRequestDTO request) {
        return trustedContactService.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        trustedContactService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
