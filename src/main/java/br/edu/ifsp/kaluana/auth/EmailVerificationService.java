package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Base64;


@Service
public class EmailVerificationService {

    public static final String INVALID_LINK = "Este link de confirmação não vale mais. Peça um novo no app.";
    private static final int TOKEN_BYTES = 32;

    private final EmailVerificationTokenRepository repository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;
    private final MailProperties properties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationService(
            EmailVerificationTokenRepository repository,
            UserRepository userRepository,
            ApplicationEventPublisher events,
            MailProperties properties,
            Clock clock) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.events = events;
        this.properties = properties;
        this.clock = clock;
    }

    public boolean confirmationRequired() {
        return properties.requireConfirmation();
    }

    
    @Transactional
    public void sendConfirmation(User user) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new EmailVerificationToken(
                user.getId(), RefreshTokenService.hash(raw), now().plusHours(properties.tokenExpirationHours())));
        events.publishEvent(new EmailConfirmationRequested(user.getName(), user.getEmail(), confirmationLink(raw)));
    }

    
    @Transactional
    public User confirm(String raw) {
        OffsetDateTime now = now();
        EmailVerificationToken token = repository.findByTokenHash(RefreshTokenService.hash(raw))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, INVALID_LINK));
        token.markUsed(now);
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.GONE, INVALID_LINK));
        user.markEmailVerified();
        return user;
    }

    
    @Transactional
    public void resend(String email) {
        userRepository.findByEmailIgnoreCase(email.trim())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user -> {
                    repository.invalidateAllOf(user.getId(), now());
                    sendConfirmation(user);
                });
    }

    private String confirmationLink(String raw) {
        String base = properties.confirmationBaseUrl().replaceAll("/+$", "");
        return base + "/api/auth/confirm-email?token=" + raw;
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    
    public record EmailConfirmationRequested(String name, String email, String link) {
    }
}
