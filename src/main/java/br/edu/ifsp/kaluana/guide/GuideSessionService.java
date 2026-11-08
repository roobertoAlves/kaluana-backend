package br.edu.ifsp.kaluana.guide;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GuideSessionService {

    private final GuideSessionRepository repository;

    public GuideSessionService(GuideSessionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public GuideSessionResponseDTO record(UUID userId, GuideSessionRequestDTO request) {
        if (request.finishedAt().isBefore(request.startedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O fim da sessão não pode ser antes do início.");
        }
        GuideSession session = new GuideSession(userId, request.guideId(), request.guideVersion(), request.status(),
                request.stepsCount(), request.startedAt(), request.finishedAt());
        return GuideSessionResponseDTO.from(repository.save(session));
    }

    @Transactional(readOnly = true)
    public List<GuideSessionResponseDTO> recent(UUID userId) {
        return repository.findTop50ByUserIdOrderByFinishedAtDesc(userId).stream().map(GuideSessionResponseDTO::from).toList();
    }
}
