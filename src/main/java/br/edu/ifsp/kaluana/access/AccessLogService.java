package br.edu.ifsp.kaluana.access;

import br.edu.ifsp.kaluana.user.User;
import br.edu.ifsp.kaluana.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccessLogService {

    private final AccessLogRepository repository;
    private final UserRepository userRepository;

    public AccessLogService(AccessLogRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void record(UUID primaryUserId, UUID viewerId, AccessAction action, int periodDays) {
        repository.save(new AccessLog(primaryUserId, viewerId, action, periodDays));
    }

    @Transactional(readOnly = true)
    public List<AccessLogDTO> recentFor(UUID primaryUserId) {
        List<AccessLog> logs = repository.findTop100ByPrimaryUserIdOrderByCreatedAtDesc(primaryUserId);
        Map<UUID, String> names = userRepository.findAllById(logs.stream().map(AccessLog::getViewerId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(User::getId, User::getName, (a, b) -> a));
        return logs.stream()
                .map(log -> new AccessLogDTO(log.getViewerId(), names.getOrDefault(log.getViewerId(), ""), log.getAction(),
                        log.getPeriodDays(), log.getCreatedAt()))
                .toList();
    }
}
