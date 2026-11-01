package br.edu.ifsp.kaluana.event;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** RF1: eventos registrados pelo próprio usuário. Toda consulta é filtrada pelo dono. */
@Service
public class LifeEventService {

    private final LifeEventRepository lifeEventRepository;

    public LifeEventService(LifeEventRepository lifeEventRepository) {
        this.lifeEventRepository = lifeEventRepository;
    }

    @Transactional(readOnly = true)
    public List<LifeEventResponseDTO> list(UUID userId) {
        return lifeEventRepository.findTop500ByUserIdOrderByOccurredAtDesc(userId).stream()
                .map(LifeEventResponseDTO::from)
                .toList();
    }

    @Transactional
    public LifeEventResponseDTO create(UUID userId, LifeEventRequestDTO request) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime occurredAt = request.timestamp() == null || request.timestamp().isAfter(now) ? now : request.timestamp();
        LifeEvent event = new LifeEvent(userId, request.category(), blankToNull(request.text()), request.urgent(), occurredAt);
        return LifeEventResponseDTO.from(lifeEventRepository.save(event));
    }

    @Transactional
    public void delete(UUID userId, UUID eventId) {
        // Busca já filtrada pelo dono: evento de outra pessoa responde 404, sem revelar que existe.
        LifeEvent event = lifeEventRepository.findByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado."));
        lifeEventRepository.delete(event);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
