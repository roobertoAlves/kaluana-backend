package br.edu.ifsp.kaluana.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LifeEventRepository extends JpaRepository<LifeEvent, UUID> {

    List<LifeEvent> findTop500ByUserIdOrderByOccurredAtDesc(UUID userId);

    Optional<LifeEvent> findByIdAndUserId(UUID id, UUID userId);

    List<LifeEvent> findTop500ByUserIdAndOccurredAtGreaterThanEqualOrderByOccurredAtAsc(UUID userId, OffsetDateTime since);

    long countByUserIdAndUrgentTrueAndOccurredAtGreaterThanEqual(UUID userId, OffsetDateTime since);

    @Query("""
            select e.category as category, count(e) as total
            from LifeEvent e
            where e.userId = :userId and e.occurredAt >= :since
            group by e.category
            """)
    List<CategoryCount> countByCategorySince(@Param("userId") UUID userId, @Param("since") OffsetDateTime since);

    interface CategoryCount {
        EventCategory getCategory();

        long getTotal();
    }
}
