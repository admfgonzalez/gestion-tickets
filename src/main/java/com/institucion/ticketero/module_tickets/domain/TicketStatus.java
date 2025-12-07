package com.institucion.ticketero.module_tickets.domain;

/**
 * Q-Insight: Domain Enum for Ticket Status.
 * Represents the lifecycle of a ticket from creation to completion.
 * This is fundamental for tracking a customer's journey and for the system's state machine.
 */
public enum TicketStatus {
    /**
     * The ticket has been created and is waiting in a queue.
     */
    PENDING,

    /**
     * The ticket has been assigned to an executive and is currently being served.
     */
    ATTENDING,

    /**
     * The customer has been served and the ticket is closed.
     */
    CLOSED,

    /**
     * The ticket was cancelled either by the system or the user.
     */
    CANCELLED
}
