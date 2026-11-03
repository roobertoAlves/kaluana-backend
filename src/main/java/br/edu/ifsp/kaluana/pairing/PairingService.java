package br.edu.ifsp.kaluana.pairing;

import br.edu.ifsp.kaluana.access.AccessSettingsService;
import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PairingService {

    // Sem O/0/I/1, para não confundir quem está digitando o código à mão.
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PairingCodeRepository pairingCodeRepository;
    private final ResponsibleLinkRepository responsibleLinkRepository;
    private final UserRepository userRepository;
    private final AccessSettingsService accessSettingsService;
    private final long codeExpirationMinutes;

    public PairingService(
            PairingCodeRepository pairingCodeRepository,
            ResponsibleLinkRepository responsibleLinkRepository,
            UserRepository userRepository,
            AccessSettingsService accessSettingsService,
            PairingProperties properties) {
        this.pairingCodeRepository = pairingCodeRepository;
        this.responsibleLinkRepository = responsibleLinkRepository;
        this.userRepository = userRepository;
        this.accessSettingsService = accessSettingsService;
        this.codeExpirationMinutes = properties.codeExpirationMinutes();
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
            responsibleLinkRepository.save(ResponsibleLink.withDefaults(
                    responsibleId, primaryUserId, accessSettingsService.find(primaryUserId)));
        }
    }

    /** Vínculos de quem está logado, com o nome de quem está do outro lado. */
    @Transactional(readOnly = true)
    public List<LinkedUserResponseDTO> listLinks(UUID userId) {
        List<ResponsibleLink> links = responsibleLinkRepository.findAllInvolving(userId);
        Map<UUID, User> others = userRepository
                .findAllById(links.stream().map(link -> link.otherSideOf(userId)).toList())
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return links.stream()
                .filter(link -> others.containsKey(link.otherSideOf(userId)))
                .map(link -> {
                    User other = others.get(link.otherSideOf(userId));
                    return new LinkedUserResponseDTO(other.getId(), other.getName(), other.getRole().name(),
                            link.getCreatedAt(), LinkPermissionsDTO.from(link));
                })
                .toList();
    }

    /** Qualquer um dos lados pode desfazer o vínculo (o principal revoga, o responsável sai). */
    @Transactional
    public void unlink(UUID userId, UUID otherUserId) {
        ResponsibleLink link = responsibleLinkRepository.findAllInvolving(userId).stream()
                .filter(l -> l.otherSideOf(userId).equals(otherUserId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vínculo não encontrado."));
        responsibleLinkRepository.delete(link);
    }

    /** RF8: o principal define o que cada pessoa vinculada pode ver. */
    @Transactional
    public LinkPermissionsDTO updatePermissions(UUID primaryUserId, UUID responsibleId, LinkPermissionsDTO permissions) {
        ResponsibleLink link = findLink(responsibleId, primaryUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vínculo não encontrado."));
        link.updatePermissions(permissions);
        return LinkPermissionsDTO.from(link);
    }

    @Transactional(readOnly = true)
    public Optional<ResponsibleLink> findLink(UUID responsibleId, UUID primaryUserId) {
        return responsibleLinkRepository.findByResponsibleIdAndPrimaryUserId(responsibleId, primaryUserId);
    }

    @Transactional(readOnly = true)
    public boolean isLinked(UUID responsibleId, UUID primaryUserId) {
        return responsibleLinkRepository.existsByResponsibleIdAndPrimaryUserId(responsibleId, primaryUserId);
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
