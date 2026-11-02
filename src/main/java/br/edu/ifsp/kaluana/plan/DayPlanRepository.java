package br.edu.ifsp.kaluana.plan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DayPlanRepository extends JpaRepository<DayPlan, UUID> {

    Optional<DayPlan> findByUserIdAndPlanDate(UUID userId, LocalDate planDate);
}
