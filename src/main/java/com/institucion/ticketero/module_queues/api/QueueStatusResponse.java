package com.institucion.ticketero.module_queues.api;

import com.institucion.ticketero.module_queues.domain.AttentionType;

/**
 * Q-Insight: API DTO Record for Queue Status.
 * Represents the real-time status of a single attention queue.
 * Used for the supervisor dashboard (RF-007) and general system monitoring.
 *
 * @param attentionType The type of the queue (e.g., PERSONAL_BANKER).
 * @param waitingCustomers The number of customers currently waiting in this queue.
 * @param averageWaitTimeMinutes The current average wait time for this queue.
 */
public record QueueStatusResponse(
    AttentionType attentionType,
    int waitingCustomers,
    long averageWaitTimeMinutes
) {}
