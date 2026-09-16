package br.edu.ifsp.kaluana.controller;

import br.edu.ifsp.kaluana.service.AuthService;
import br.edu.ifsp.kaluana.dto.AuthResponse;
import br.edu.ifsp.kaluana.dto.LoginRequest;
import br.edu.ifsp.kaluana.dto.RegisterPrimaryRequest;
import br.edu.ifsp.kaluana.dto.RegisterResponsibleRequest;
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
    public ResponseEntity<AuthResponse> registerPrimary(@Valid @RequestBody RegisterPrimaryRequest request){
       return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerPrimary(request));
    }

    @PostMapping("/register/responsible")
    public ResponseEntity<AuthResponse> registerResponsible(@Valid @RequestBody RegisterResponsibleRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerResponsible(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}
