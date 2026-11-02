package br.edu.ifsp.kaluana.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/social")
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    public SocialAuthController(SocialAuthService socialAuthService) {
        this.socialAuthService = socialAuthService;
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleLogin(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        return ResponseEntity.ok(socialAuthService.authenticateGoogle(token));
    }

    @PostMapping("/github")
    public ResponseEntity<AuthResponseDTO> githubLogin(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        return ResponseEntity.ok(socialAuthService.authenticateGithub(token));
    }
}
