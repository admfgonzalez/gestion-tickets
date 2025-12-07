package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_queues.domain.AttentionType;

import java.util.Set;
import java.util.UUID;

/**
 * Q-Insight: API DTO Record for Executive Status.
 * Represents the real-time status of a single service executive.
 * Used as part of the supervisor dashboard (RF-007).
 *
 * @param id The executive's unique ID.
 * @param fullName The executive's name.
 * @param module The executive's service module.
 * @param status The current status (AVAILABLE, BUSY).
 * @param currentTicketNumber The number of the ticket the executive is currently serving, if any.
 * @param supportedAttentionTypes The set of attention types this executive can handle.
 */
public record ExecutiveStatusResponse(
    UUID id,
    String fullName,
    String module,
    ExecutiveStatus status,
    String currentTicketNumber,
    Set<AttentionType> supportedAttentionTypes
) {}
