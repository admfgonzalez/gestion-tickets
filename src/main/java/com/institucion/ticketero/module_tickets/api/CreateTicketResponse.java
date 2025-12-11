package com.institucion.ticketero.module_tickets.api;

import com.institucion.ticketero.module_queues.domain.AttentionType;

import java.util.UUID;

/**
 * Q-Insight: API DTO Record for Ticket Creation Response.
 * This record models the data sent back to the client after a ticket is successfully created.
 * It provides the essential information the customer needs immediately (RF-001).
 *
 * @param codigoReferencia The unique identifier for the ticket.
 * @param ticketNumber The human-readable number assigned to the ticket.
 * @param positionInQueue The customer's initial position in the queue.
 * @param estimatedWaitMinutes The estimated waiting time in minutes.
 * @param queueType The type of queue the ticket belongs to.
 */
public record CreateTicketResponse(
    UUID codigoReferencia,
    String ticketNumber,
    int positionInQueue,
    long estimatedWaitMinutes,
    AttentionType queueType
) {}
