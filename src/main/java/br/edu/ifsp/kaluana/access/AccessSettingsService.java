package br.edu.ifsp.kaluana.access;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public AccessSettings find(UUID userId) {
        return accessSettingsRepository.findById(userId)
                .orElseGet(() -> AccessSettings.nothingSharedFor(userId));
    }
}
