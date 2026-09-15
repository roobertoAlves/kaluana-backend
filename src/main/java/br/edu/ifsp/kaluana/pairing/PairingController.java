package br.edu.ifsp.kaluana.pairing;

import br.edu.ifsp.kaluana.pairing.dto.GenerateCodeResponse;
import br.edu.ifsp.kaluana.pairing.dto.ValidateCodeRequest;
import br.edu.ifsp.kaluana.pairing.dto.ValidateCodeResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/pairing")
public class PairingController {

    private final PairingService pairingService;

    public PairingController(PairingService pairingService) {
        this.pairingService = pairingService;
    }

    @PostMapping("/validate-code")
    public ResponseEntity<ValidateCodeResponse> validateCode(@Valid @RequestBody ValidateCodeRequest request) {
        return ResponseEntity.ok(pairingService.validateCode(request.code()));
    }

    @PostMapping("/gerar-codigo")
    @PreAuthorize("hasRole('PRIMARY')")
    public ResponseEntity<GenerateCodeResponse> generateCode(Authentication authentication) {
        UUID primaryUserId = (UUID) authentication.getPrincipal();
        return ResponseEntity.ok(pairingService.generateCode(primaryUserId));
    }

}
