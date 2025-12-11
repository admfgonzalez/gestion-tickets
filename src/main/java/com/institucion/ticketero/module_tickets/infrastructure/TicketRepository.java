package com.institucion.ticketero.module_tickets.infrastructure;

import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Q-Insight: Infrastructure Repository for Tickets.
 * This interface provides the data access layer for the Ticket entity, powered by Spring Data JPA.
 * It abstracts away the boilerplate code for database operations.
 * Methods are automatically implemented by Spring based on their names.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    /**
     * Q-Insight: Counts tickets by status and type.
     * A crucial query for calculating a customer's position in a queue (RF-003).
     * It counts how many tickets of the same attention type were created before the given ticket's creation time.
     */
    long countByAttentionTypeAndStatusAndCreatedAtBefore(
            AttentionType attentionType, TicketStatus status, LocalDateTime createdAt);

    /**
     * Q-Insight: Finds the next available ticket in a queue.
     * This query is the core of the ticket assignment logic (RF-004).
     * It finds the oldest ticket with PENDING status for a given attention type, respecting FIFO order.
     */
    Optional<Ticket> findFirstByAttentionTypeAndStatusOrderByCreatedAtAsc(
            AttentionType attentionType, TicketStatus status);

    /**
     * Q-Insight: Finds a ticket by its human-readable number.
     * Used for the customer-facing status check functionality (RF-006).
     */
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    
    /**
     * Q-Insight: Counts tickets by their current status.
     * Used for generating dashboard metrics (RF-007).
     */
    long countByStatus(TicketStatus status);

    /**
     * Q-Insight: Counts tickets created within a specific time window.
     * Used for the "total tickets today" metric on the dashboard (RF-007).
     */
    long countByCreatedAtAfter(LocalDateTime startTime);

    /**
     * Q-Insight: Counts tickets for a specific type created after a certain time.
     * This is the cornerstone of the new daily-sequential ticket number generation logic.
     * It allows us to find out how many 'CAJA' tickets, for example, have been created today.
     * @param attentionType The type of attention.
     * @param startTime The start of the time window (e.g., beginning of the day).
     * @return The count of matching tickets.
     */
    long countByAttentionTypeAndCreatedAtAfter(AttentionType attentionType, LocalDateTime startTime);

    /**
     * Q-Insight: Counts tickets for a specific attention type and workday.
     * This is used for generating sequential ticket numbers unique per workday.
     * @param attentionType The type of attention.
     * @param workdayId The ID of the workday.
     * @return The count of matching tickets.
     */
    long countByAttentionTypeAndWorkdayId(AttentionType attentionType, UUID workdayId);

    /**
     * Q-Insight: Counts tickets by status, attention type, workday, and creation time.
     * A crucial query for calculating a customer's position in a queue, now scoped per workday.
     * It counts how many tickets of the same attention type and workday were created before the given ticket's creation time.
     */
    long countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore(
            AttentionType attentionType, UUID workdayId, TicketStatus status, LocalDateTime createdAt);

    /**
     * Q-Insight: Finds tickets that require a pre-arrival notification.
     * This query selects tickets that are 'nth' in line, triggering the "pre-aviso" notification (RF-002).
     * The native query is complex because it needs to calculate the rank of tickets within each queue type.
     * @param position The position in the queue to check for (e.g., 3).
     */
    @Query(value = """
        SELECT t.* FROM (
            SELECT *, RANK() OVER (PARTITION BY attention_type ORDER BY created_at ASC) as rnk
            FROM tickets
            WHERE status = 'PENDING'
        ) t
        WHERE t.rnk = :position
    """, nativeQuery = true)
    List<Ticket> findTicketsAtPositionInQueue(int position);

    List<Ticket> findByExecutiveIdAndStatus(UUID executiveId, TicketStatus status);

    /**
     * Finds all tickets with a given status, ordered by when they were attended.
     * @param status The status to filter by.
     * @return A list of tickets.
     */
    List<Ticket> findAllByStatusOrderByAttendedAtDesc(TicketStatus status);

    /**
     * Finds all tickets created within a given time range.
     * @param startTime The start of the time range.
     * @param endTime The end of the time range.
     * @return A list of tickets.
     */
    List<Ticket> findAllByCreatedAtBetweenOrderByCreatedAtAsc(LocalDateTime startTime, LocalDateTime endTime);
}
