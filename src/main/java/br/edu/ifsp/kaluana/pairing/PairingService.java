package br.edu.ifsp.kaluana.pairing;

import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PairingService {

    // Sem O/0/I/1, para não confundir quem está digitando o código à mão.
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PairingCodeRepository pairingCodeRepository;
    private final ResponsibleLinkRepository responsibleLinkRepository;
    private final UserRepository userRepository;
    private final long codeExpirationMinutes;

    public PairingService(
            PairingCodeRepository pairingCodeRepository,
            ResponsibleLinkRepository responsibleLinkRepository,
            UserRepository userRepository,
            @Value("${app.pairing.code-expiration-minutes}") long codeExpirationMinutes) {
        this.pairingCodeRepository = pairingCodeRepository;
        this.responsibleLinkRepository = responsibleLinkRepository;
        this.userRepository = userRepository;
        this.codeExpirationMinutes = codeExpirationMinutes;
    }

    @Transactional
    public GenerateCodeResponseDTO generateCode(UUID primaryUserId) {
        String code = generateUniqueCode();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(codeExpirationMinutes);
        pairingCodeRepository.save(new PairingCode(code, primaryUserId, expiresAt));
        return new GenerateCodeResponseDTO(code, expiresAt);
    }

    @Transactional(readOnly = true)
    public ValidateCodeResponseDTO validateCode(String code) {
        PairingCode pairingCode = findValidCode(code);
        User primaryUser = findUser(pairingCode.getPrimaryUserId());
        return new ValidateCodeResponseDTO(true, primaryUser.getName());
    }

    /**
     * Revalida e consome o código de forma atômica. Quem chama este método
     * (AuthService) faz isso dentro da mesma transação em que cria a conta do
     * responsável, então, se o cadastro falhar depois, o código volta a ficar
     * disponível (rollback), em vez de ser desperdiçado.
     */
    @Transactional
    public UUID consumeCode(String code) {
        PairingCode pairingCode = findValidCode(code);
        pairingCode.markAsUsed();
        pairingCodeRepository.save(pairingCode);
        return pairingCode.getPrimaryUserId();
    }

    @Transactional
    public void createLink(UUID responsibleId, UUID primaryUserId) {
        if (!responsibleLinkRepository.existsByResponsibleIdAndPrimaryUserId(responsibleId, primaryUserId)) {
            responsibleLinkRepository.save(new ResponsibleLink(responsibleId, primaryUserId));
        }
    }

    private PairingCode findValidCode(String code) {
        return pairingCodeRepository
                .findByCodeAndUsedFalseAndExpiresAtAfter(code, OffsetDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.GONE,
                        "Código inválido ou expirado. Peça um novo à pessoa principal."));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    private String generateUniqueCode() {
        String code;
        OffsetDateTime now = OffsetDateTime.now();
        do {
            code = generateRandomCode();
        } while (pairingCodeRepository.findByCodeAndUsedFalseAndExpiresAtAfter(code, now).isPresent());
        return code;
    }

    private String generateRandomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}
