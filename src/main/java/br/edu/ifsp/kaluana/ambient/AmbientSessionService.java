package br.edu.ifsp.kaluana.ambient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** RF16: histórico das sessões de Relaxar e Dormir (indicador de uso, RF8). */
@Service
public class AmbientSessionService {

    private final AmbientSessionRepository ambientSessionRepository;

    public AmbientSessionService(AmbientSessionRepository ambientSessionRepository) {
        this.ambientSessionRepository = ambientSessionRepository;
    }

    @Transactional(readOnly = true)
    public List<AmbientSessionResponseDTO> list(UUID userId) {
        return ambientSessionRepository.findTop100ByUserIdOrderByFinishedAtDesc(userId).stream()
                .map(AmbientSessionResponseDTO::from)
                .toList();
    }

    @Transactional
    public AmbientSessionResponseDTO register(UUID userId, AmbientSessionRequestDTO request) {
        AmbientSession session = new AmbientSession(
                userId,
                request.mode(),
                request.sound(),
                request.plannedMinutes(),
                request.status(),
                request.startedAt(),
                request.finishedAt());
        return AmbientSessionResponseDTO.from(ambientSessionRepository.save(session));
    }
}
