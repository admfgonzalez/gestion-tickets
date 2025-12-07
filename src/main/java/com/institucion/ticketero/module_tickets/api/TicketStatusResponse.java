package com.institucion.ticketero.module_tickets.api;

import com.institucion.ticketero.module_tickets.domain.TicketStatus;

/**
 * Q-Insight: API DTO Record for Ticket Status Query.
 * This record provides a detailed view of a ticket's current status (RF-006).
 * It decouples the API representation from the underlying `Ticket` domain entity.
 *
 * @param ticketNumber The ticket's reference number.
 * @param status The current status (e.g., PENDING, ATTENDING).
 * @param queuePosition Current position in the queue (0 if not pending).
 * @param estimatedWaitTimeMinutes Updated estimated wait time.
 * @param assignedExecutiveName The name of the assigned executive, if applicable.
 * @param executiveModule The service module of the assigned executive, if applicable.
 */
public record TicketStatusResponse(
    String ticketNumber,
    TicketStatus status,
    int queuePosition,
    long estimatedWaitTimeMinutes,
    String assignedExecutiveName,
    String executiveModule
) {}
