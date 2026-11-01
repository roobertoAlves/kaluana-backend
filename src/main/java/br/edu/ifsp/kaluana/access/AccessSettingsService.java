package br.edu.ifsp.kaluana.access;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** RF8: preferências de compartilhamento do usuário principal. */
@Service
public class AccessSettingsService {

    private final AccessSettingsRepository accessSettingsRepository;

    public AccessSettingsService(AccessSettingsRepository accessSettingsRepository) {
        this.accessSettingsRepository = accessSettingsRepository;
    }

    @Transactional(readOnly = true)
    public AccessSettingsDTO get(UUID userId) {
        return AccessSettingsDTO.from(find(userId));
    }

    @Transactional
    public AccessSettingsDTO update(UUID userId, AccessSettingsDTO request) {
        AccessSettings settings = find(userId);
        settings.update(request.events(), request.checkins(), request.usage(), request.reports());
        return AccessSettingsDTO.from(accessSettingsRepository.save(settings));
    }

    /** Usado por outros serviços (ex.: relatório) para aplicar o RNF3. */
    @Transactional(readOnly = true)
    public AccessSettings find(UUID userId) {
        return accessSettingsRepository.findById(userId)
                .orElseGet(() -> AccessSettings.nothingSharedFor(userId));
    }
}
