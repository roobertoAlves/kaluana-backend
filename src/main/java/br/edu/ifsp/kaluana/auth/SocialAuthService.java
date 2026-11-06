package br.edu.ifsp.kaluana.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class SocialAuthService {

    public AuthResponseDTO authenticateGoogle(String token) {
        throw notImplemented("Google");
    }

    public AuthResponseDTO authenticateGithub(String token) {
        throw notImplemented("GitHub");
    }

    private static ResponseStatusException notImplemented(String provider) {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Autenticação via " + provider + " ainda não implementada.");
    }
}
