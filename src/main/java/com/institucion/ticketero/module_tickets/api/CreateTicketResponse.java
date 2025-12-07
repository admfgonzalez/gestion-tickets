package com.institucion.ticketero.module_tickets.api;

/**
 * Q-Insight: API DTO Record for Ticket Creation Response.
 * This record models the data sent back to the client after a ticket is successfully created.
 * It provides the essential information the customer needs immediately (RF-001).
 *
 * @param ticketNumber The human-readable number assigned to the ticket.
 * @param queuePosition The customer's initial position in the queue.
 * @param estimatedWaitTimeMinutes The estimated waiting time in minutes.
 */
public record CreateTicketResponse(
    String ticketNumber,
    int queuePosition,
    long estimatedWaitTimeMinutes
) {}
