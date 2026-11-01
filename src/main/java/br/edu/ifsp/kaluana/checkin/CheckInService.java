package br.edu.ifsp.kaluana.checkin;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** RF3: um check-in por dia, consultado como série temporal. */
@Service
public class CheckInService {

    // Folga para fusos à frente do servidor (ex.: usuário em UTC+14).
    private static final int MAX_DAYS_AHEAD = 1;

    private final CheckInRepository checkInRepository;

    public CheckInService(CheckInRepository checkInRepository) {
        this.checkInRepository = checkInRepository;
    }

    @Transactional(readOnly = true)
    public List<CheckInResponseDTO> list(UUID userId) {
        return checkInRepository.findTop366ByUserIdOrderByDayDesc(userId).stream()
                .map(CheckInResponseDTO::from)
                .toList();
    }

    /** Idempotente: cria o check-in do dia ou substitui o nível já registrado. */
    @Transactional
    public CheckInResponseDTO record(UUID userId, LocalDate day, CheckInRequestDTO request) {
        if (day.isAfter(LocalDate.now().plusDays(MAX_DAYS_AHEAD))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível registrar check-in de um dia futuro.");
        }

        CheckIn checkIn = checkInRepository.findByUserIdAndDay(userId, day)
                .orElseGet(() -> new CheckIn(userId, day));
        checkIn.record(request.level());
        return CheckInResponseDTO.from(checkInRepository.save(checkIn));
    }

    /** Nível registrado no dia, se houver. Usado pelo plano do dia (RF10). */
    @Transactional(readOnly = true)
    public Optional<Integer> levelOn(UUID userId, LocalDate day) {
        return checkInRepository.findByUserIdAndDay(userId, day).map(CheckIn::getLevel);
    }
}
