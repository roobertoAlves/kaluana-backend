package br.edu.ifsp.kaluana.access;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/access-settings")
@PreAuthorize("hasRole('PRIMARY')")
public class AccessSettingsController {

    private final AccessSettingsService accessSettingsService;

    public AccessSettingsController(AccessSettingsService accessSettingsService) {
        this.accessSettingsService = accessSettingsService;
    }

    @GetMapping
    public AccessSettingsDTO get(@AuthenticationPrincipal UUID userId) {
        return accessSettingsService.get(userId);
    }

    @PutMapping
    public AccessSettingsDTO update(@AuthenticationPrincipal UUID userId, @RequestBody AccessSettingsDTO request) {
        return accessSettingsService.update(userId, request);
    }
}
