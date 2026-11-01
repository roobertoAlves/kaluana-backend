package br.edu.ifsp.kaluana.pairing;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/pairing")
public class PairingController {

    private final PairingService pairingService;

    public PairingController(PairingService pairingService) {
        this.pairingService = pairingService;
    }

    @PostMapping("/validate-code")
    public ResponseEntity<ValidateCodeResponseDTO> validateCode(@Valid @RequestBody ValidateCodeRequestDTO request) {
        return ResponseEntity.ok(pairingService.validateCode(request.code()));
    }

    @PostMapping("/generate-code")
    @PreAuthorize("hasRole('PRIMARY')")
    public ResponseEntity<GenerateCodeResponseDTO> generateCode(Authentication authentication) {
        UUID primaryUserId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(pairingService.generateCode(primaryUserId));
    }

    @GetMapping("/links")
    public List<LinkedUserResponseDTO> listLinks(@AuthenticationPrincipal UUID userId) {
        return pairingService.listLinks(userId);
    }

    @DeleteMapping("/links/{otherUserId}")
    public ResponseEntity<Void> unlink(@AuthenticationPrincipal UUID userId, @PathVariable UUID otherUserId) {
        pairingService.unlink(userId, otherUserId);
        return ResponseEntity.noContent().build();
    }

}
