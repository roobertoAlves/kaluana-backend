package br.edu.ifsp.kaluana.auth;


import br.edu.ifsp.kaluana.auth.dto.AuthResponse;
import br.edu.ifsp.kaluana.auth.dto.LoginRequest;
import br.edu.ifsp.kaluana.auth.dto.RegisterPrimaryRequest;
import br.edu.ifsp.kaluana.auth.dto.RegisterResponsibleRequest;
import br.edu.ifsp.kaluana.auth.dto.UserResponse;
import br.edu.ifsp.kaluana.pairing.PairingService;
import br.edu.ifsp.kaluana.security.JwtService;
import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRepository;
import br.edu.ifsp.kaluana.user.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class AuthService {
}
