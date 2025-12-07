package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;

/**
 * Q-Insight: API DTO Record for Executive Status.
 * Represents the real-time status of a single service executive.
 * Used as part of the supervisor dashboard (RF-007).
 *
 * @param fullName The executive's name.
 * @param module The executive's service module.
 * @param status The current status (AVAILABLE, BUSY).
 * @param currentTicketNumber The number of the ticket the executive is currently serving, if any.
 */
public record ExecutiveStatusResponse(
    String fullName,
    String module,
    ExecutiveStatus status,
    String currentTicketNumber
) {}
