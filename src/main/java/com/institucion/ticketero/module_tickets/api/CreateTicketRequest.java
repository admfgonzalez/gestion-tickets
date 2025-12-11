package com.institucion.ticketero.module_tickets.api;

import com.institucion.ticketero.module_queues.domain.AttentionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Q-Insight: API DTO Record for Ticket Creation.
 * This record models the incoming request to create a new ticket (RF-001).
 * It uses Jakarta Bean Validation annotations (@NotBlank, @NotNull) for automatic input validation at the controller level.
 * Using a record provides immutability, conciseness, and clear intent for data transfer.
 *
 * @param nationalId The customer's unique identifier (e.g., RUT).
 * @param attentionType The desired type of service (e.g., CAJA).
 * @param telefono Optional phone number for notifications.
 * @param branchOffice The branch office where the ticket is being created.
 */
public record CreateTicketRequest(
    @NotBlank(message = "National ID cannot be blank.")
    String nationalId,

    @NotNull(message = "Attention type must be specified.")
    AttentionType attentionType,

    String telefono,
    
    @NotBlank(message = "Branch office cannot be blank.")
    String branchOffice
) {}
