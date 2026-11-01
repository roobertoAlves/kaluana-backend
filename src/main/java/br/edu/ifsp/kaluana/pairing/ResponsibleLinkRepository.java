package br.edu.ifsp.kaluana.pairing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResponsibleLinkRepository extends JpaRepository<ResponsibleLink, UUID>{
    boolean existsByResponsibleIdAndPrimaryUserId(UUID responsibleId, UUID primaryUserId);

    @Query("""
            select l from ResponsibleLink l
            where l.primaryUserId = :userId or l.responsibleId = :userId
            order by l.createdAt
            """)
    List<ResponsibleLink> findAllInvolving(@Param("userId") UUID userId);
}
