package br.edu.ifsp.kaluana.contact;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/** RF4: contatos de confiança com a forma de contato preferida. */
@Service
public class TrustedContactService {

    private static final Pattern EMAIL = Pattern.compile("^\\S+@\\S+\\.\\S+$");
    private static final int MIN_PHONE_DIGITS = 10;

    private final TrustedContactRepository trustedContactRepository;

    public TrustedContactService(TrustedContactRepository trustedContactRepository) {
        this.trustedContactRepository = trustedContactRepository;
    }

    @Transactional(readOnly = true)
    public List<TrustedContactResponseDTO> list(UUID userId) {
        return trustedContactRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(TrustedContactResponseDTO::from)
                .toList();
    }

    @Transactional
    public TrustedContactResponseDTO create(UUID userId, TrustedContactRequestDTO request) {
        TrustedContact contact = new TrustedContact(userId);
        applyRequest(contact, request);
        return TrustedContactResponseDTO.from(trustedContactRepository.save(contact));
    }

    @Transactional
    public TrustedContactResponseDTO update(UUID userId, UUID contactId, TrustedContactRequestDTO request) {
        TrustedContact contact = findOwned(userId, contactId);
        applyRequest(contact, request);
        return TrustedContactResponseDTO.from(contact);
    }

    @Transactional
    public void delete(UUID userId, UUID contactId) {
        trustedContactRepository.delete(findOwned(userId, contactId));
    }

    private void applyRequest(TrustedContact contact, TrustedContactRequestDTO request) {
        String value = request.value().trim();
        validateValue(request, value);
        contact.update(request.name().trim(), request.method(), value);
    }

    // Mesma regra do app: telefone com DDD (10+ dígitos) ou e-mail no formato básico.
    private void validateValue(TrustedContactRequestDTO request, String value) {
        if (request.method().isPhone() && value.replaceAll("\\D", "").length() < MIN_PHONE_DIGITS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o telefone com DDD.");
        }
        if (!request.method().isPhone() && !EMAIL.matcher(value).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail inválido.");
        }
    }

    private TrustedContact findOwned(UUID userId, UUID contactId) {
        return trustedContactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contato não encontrado."));
    }
}
