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
    EN_ESPERA,

    /**
     * The ticket is about to be attended.
     */
    PROXIMO,

    /**
     * The ticket has been assigned to an executive and is currently being served.
     */
    ATENDIENDO,

    /**
     * The customer has been served and the ticket is closed.
     */
    COMPLETADO,

    /**
     * The ticket was cancelled either by the system or the user.
     */
    CANCELADO,

    /**
     * The customer did not show up when called.
     */
    NO_ATENDIDO
}
