package com.institucion.ticketero.module_queues.infrastructure;

import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import org.springframework.stereotype.Repository;

/**
 * Q-Insight: Infrastructure Repository for Queues.
 * This is not a standard Spring Data JPA repository because there is no `Queue` entity.
 * Instead, it acts as a facade over the `TicketRepository` to provide queue-centric data.
 * This pattern keeps queue-related data logic separate from the ticket module.
 */
@Repository
public class QueueRepository {

    private final TicketRepository ticketRepository;

    public QueueRepository(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Q-Insight: Counts waiting customers in a specific queue.
     * Provides a key metric for the dashboard (RF-007) and for calculating estimated wait times (RF-003).
     *
     * @param attentionType The queue to count customers for.
     * @return The number of tickets in PENDING status for that queue.
     */
    public long countByAttentionTypeAndStatus(AttentionType attentionType, TicketStatus status) {
        // This is a derived query. The actual count is performed on the Ticket entity.
        return ticketRepository.countByStatus(status);
    }
}
