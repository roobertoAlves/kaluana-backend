package br.edu.ifsp.kaluana.security;

import br.edu.ifsp.kaluana.dto.AuthResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class SocialAuthService {

    public AuthResponseDTO authenticateGoogle(String token) {
        throw new UnsupportedOperationException("Autenticação via Google ainda não implementada.");
    }

    public AuthResponseDTO authenticateGithub(String token) {
        throw new UnsupportedOperationException("Autenticação via GitHub ainda não implementada.");
    }
}
