package com.institucion.ticketero.module_queues.api;

import com.institucion.ticketero.module_executives.api.ExecutiveStatusResponse;
import com.institucion.ticketero.module_queues.domain.AttentionType;

import java.util.List;
import java.util.Map;

/**
 * Q-Insight: API DTO Record for the Supervisor Dashboard.
 * This record aggregates all the necessary real-time metrics for the supervisor dashboard (RF-007).
 * It's a composite DTO that combines information from multiple domain modules.
 *
 * @param totalTicketsToday Total number of tickets created today.
 * @param ticketsByStatus A map showing the count of tickets for each status (PENDING, ATTENDING, etc.).
 * @param queueDetails A list containing the detailed status of each queue.
 * @param executiveDetails A list containing the detailed status of each executive.
 * @param allAttentionTypes A list of all possible attention types available in the system.
 */
public record DashboardMetricsResponse(
    long totalTicketsToday,
    Map<String, Long> ticketsByStatus,
    List<QueueStatusResponse> queueDetails,
    List<ExecutiveStatusResponse> executiveDetails,
    List<AttentionType> allAttentionTypes
) {}
