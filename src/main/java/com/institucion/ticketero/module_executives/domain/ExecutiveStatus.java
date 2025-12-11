package com.institucion.ticketero.module_executives.domain;

/**
 * Q-Insight: Domain Enum for Executive Status.
 * Represents the availability of a service executive.
 * This status is critical for the ticket assignment logic (RF-004).
 */
public enum ExecutiveStatus {
    /**
     * The executive is available to be assigned a new ticket.
     */
    AVAILABLE,

    /**
     * The executive is currently serving a customer.
     */
    BUSY,

    /**
     * The executive is not available for assignment (e.g., on a break).
     */
    OFFLINE
}
