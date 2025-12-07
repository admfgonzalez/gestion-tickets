package com.institucion.ticketero.module_queues.api;

/**
 * DTO for the simple "Now Serving" dashboard.
 * @param ticketNumber The ticket number (e.g., "CA-101").
 * @param module The module/desk where the customer is being served.
 */
public record NowServingTicket(String ticketNumber, String module) {
}
