package com.institucion.ticketero.module_executives.infrastructure;

import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Q-Insight: Infrastructure Repository for Executives.
 * This interface provides the data access layer for the Executive entity using Spring Data JPA.
 */
@Repository
public interface ExecutiveRepository extends JpaRepository<Executive, Long> {

    /**
     * Q-Insight: Finds an available executive for a specific queue type.
     * This is a key query for the automatic ticket assignment algorithm (RF-004).
     * It looks for an executive who is AVAILABLE and supports the required AttentionType.
     * The `findFirst` part implies a selection strategy (e.g., could be ordered by `lastStatusChange` for load balancing).
     */
    Optional<Executive> findFirstByStatusAndSupportedAttentionTypesContainingOrderByLastStatusChangeAsc(
            ExecutiveStatus status, AttentionType attentionType);

    List<Executive> findAllByStatus(ExecutiveStatus status);
}
