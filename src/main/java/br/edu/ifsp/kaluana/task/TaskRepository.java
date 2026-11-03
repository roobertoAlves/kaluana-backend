package br.edu.ifsp.kaluana.task;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @EntityGraph(attributePaths = "steps")
    List<Task> findTop300ByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "steps")
    List<Task> findTop300ByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, TaskStatus status);

    @EntityGraph(attributePaths = "steps")
    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Query("""
            select t from Task t
            where t.userId = :userId and t.status = br.edu.ifsp.kaluana.task.TaskStatus.TODO
              and (t.dueDate is null or t.dueDate <= :day)
            """)
    List<Task> findPlannable(@Param("userId") UUID userId, @Param("day") LocalDate day);

    @Query("""
            select t.id from Task t
            where t.userId = :userId and t.status = br.edu.ifsp.kaluana.task.TaskStatus.DONE and t.id in :ids
            """)
    List<UUID> findDoneIds(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);

    List<Task> findTop300ByUserIdAndStatusAndCompletedAtGreaterThanEqualOrderByCompletedAtAsc(UUID userId, TaskStatus status, OffsetDateTime since);

    long countByUserIdAndStatus(UUID userId, TaskStatus status);

    long countByUserIdAndStatusAndCompletedAtGreaterThanEqual(UUID userId, TaskStatus status, OffsetDateTime since);
}
