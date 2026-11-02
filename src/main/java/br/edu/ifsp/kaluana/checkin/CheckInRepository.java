package br.edu.ifsp.kaluana.checkin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    List<CheckIn> findTop366ByUserIdOrderByDayDesc(UUID userId);

    List<CheckIn> findByUserIdAndDayGreaterThanEqualOrderByDayAsc(UUID userId, LocalDate since);

    Optional<CheckIn> findByUserIdAndDay(UUID userId, LocalDate day);
}
