package br.edu.ifsp.kaluana.checkin;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CheckInService {

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

    @Transactional(readOnly = true)
    public Optional<Integer> levelOn(UUID userId, LocalDate day) {
        return checkInRepository.findByUserIdAndDay(userId, day).map(CheckIn::getLevel);
    }
}
