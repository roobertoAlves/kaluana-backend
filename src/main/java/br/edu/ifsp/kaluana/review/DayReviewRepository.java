package br.edu.ifsp.kaluana.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DayReviewRepository extends JpaRepository<DayReview, UUID> {

    Optional<DayReview> findByUserIdAndReviewDate(UUID userId, LocalDate reviewDate);

    long countByUserIdAndReviewDateGreaterThanEqual(UUID userId, LocalDate since);
}
