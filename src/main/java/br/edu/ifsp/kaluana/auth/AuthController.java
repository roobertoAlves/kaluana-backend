package br.edu.ifsp.kaluana.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/primary")
    public ResponseEntity<AuthResponseDTO> registerPrimary(@Valid @RequestBody RegisterPrimaryRequestDTO request){
       return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerPrimary(request));
    }

    @PostMapping("/register/responsible")
    public ResponseEntity<AuthResponseDTO> registerResponsible(@Valid @RequestBody RegisterResponsibleRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerResponsible(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request){
        return ResponseEntity.ok(authService.login(request));
    }
}
