package br.edu.ifsp.kaluana.auth;

import br.edu.ifsp.kaluana.security.JwtProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * RNF4: renovação da sessão com rotação. Cada uso troca o token por outro; um
 * token já trocado que reaparece indica cópia indevida, e aí todas as sessões
 * da pessoa são encerradas.
 */
@Service
public class RefreshTokenService {

    public static final String SESSION_EXPIRED = "Sua sessão expirou. Entre novamente.";
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final long expirationDays;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository, JwtProperties properties, Clock clock) {
        this.repository = repository;
        this.expirationDays = properties.refreshTokenExpirationDays();
        this.clock = clock;
    }

    /** Cria um token novo para a pessoa e devolve o valor em claro (só o app o guarda). */
    @Transactional
    public String issue(UUID userId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new RefreshToken(userId, hash(raw), now().plusDays(expirationDays)));
        return raw;
    }

    /** Troca um token válido por outro. Devolve o dono e o token novo. */
    @Transactional
    public Rotated rotate(String raw) {
        RefreshToken current = repository.findByTokenHash(hash(raw)).orElseThrow(RefreshTokenService::expired);
        OffsetDateTime now = now();
        if (current.isRevoked()) {
            repository.revokeAllOf(current.getUserId(), now);
            throw expired();
        }
        if (!current.isUsable(now)) {
            throw expired();
        }
        current.revoke(now);
        return new Rotated(current.getUserId(), issue(current.getUserId()));
    }

    /** Sair: o token deixa de valer (sem erro se já não valia). */
    @Transactional
    public void revoke(String raw) {
        repository.findByTokenHash(hash(raw)).ifPresent(t -> t.revoke(now()));
    }

    public record Rotated(UUID userId, String refreshToken) {
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private static ResponseStatusException expired() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, SESSION_EXPIRED);
    }
}
