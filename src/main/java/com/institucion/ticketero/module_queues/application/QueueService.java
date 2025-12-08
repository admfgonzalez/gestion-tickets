package com.institucion.ticketero.module_queues.application;

import com.institucion.ticketero.module_queues.api.QueueStatusResponse;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Q-Insight: Application Service for Queues.
 * This service provides logic for querying the state of the attention queues.
 * It acts as an abstraction layer over the underlying data repositories.
 */
@Service
public class QueueService {

    private final TicketRepository ticketRepository;

    public QueueService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Q-Insight: Gets the status of all queues.
     * This method calculates real-time metrics for each attention type, as required for the dashboard (RF-007).
     * @param workdayStartTime Optional start time of the active workday to scope metrics.
     * @return A list of DTOs, each representing the status of one queue.
     */
    public List<QueueStatusResponse> getAllQueueStatus(Optional<LocalDateTime> workdayStartTime) {
        return Arrays.stream(AttentionType.values())
                .map(attentionType -> getQueueStatus(attentionType, workdayStartTime))
                .collect(Collectors.toList());
    }

    /**
     * Q-Insight: Gets the status of a single queue.
     * @param attentionType The specific queue to query.
     * @param workdayStartTime Optional start time of the active workday to scope metrics.
     * @return A DTO with the metrics for the requested queue.
     */
    public QueueStatusResponse getQueueStatus(AttentionType attentionType, Optional<LocalDateTime> workdayStartTime) {
        LocalDateTime filterStartTime = workdayStartTime.orElse(java.time.LocalDate.now().atStartOfDay());

        long waitingCustomers = ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(attentionType, TicketStatus.PENDING, filterStartTime);
        long averageWaitTime = calculateAverageWaitTime(attentionType, waitingCustomers);
        return new QueueStatusResponse(attentionType, (int) waitingCustomers, averageWaitTime);
    }

    /**
     * Q-Insight: Calculates the estimated wait time for a queue.
     * Implements the core logic for RF-003. The formula is: (customers in queue) * (avg service time per type).
     * @param attentionType The queue for which to calculate the wait time.
     * @param waitingCustomers The number of customers currently in that queue.
     * @return The estimated wait time in minutes.
     */
    public long calculateAverageWaitTime(AttentionType attentionType, long waitingCustomers) {
        if (waitingCustomers == 0) {
            return 0;
        }
        // This is a simplified estimation. A more complex model could consider the number of available executives.
        return waitingCustomers * attentionType.getAverageServiceTimeMinutes();
    }
}
