package com.institucion.ticketero.module_queues.application;

import com.institucion.ticketero.module_executives.api.ExecutiveStatusResponse;
import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_executives.infrastructure.ExecutiveRepository;
import com.institucion.ticketero.module_queues.api.DashboardMetricsResponse;
import com.institucion.ticketero.module_queues.api.QueueStatusResponse;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import com.institucion.ticketero.module_workday.application.WorkdayService; // New import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional; // New import
import java.util.stream.Collectors;

/**
 * Q-Insight: Application Service for the Dashboard.
 * This service is responsible for aggregating data from various sources to build the comprehensive
 * metrics model required by the supervisor dashboard (RF-007).
 */
@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final ExecutiveRepository executiveRepository;
    private final QueueService queueService;
    private final WorkdayService workdayService; // New field

    public DashboardService(TicketRepository ticketRepository, ExecutiveRepository executiveRepository, QueueService queueService, WorkdayService workdayService) {
        this.ticketRepository = ticketRepository;
        this.executiveRepository = executiveRepository;
        this.queueService = queueService;
        this.workdayService = workdayService; // Inject
    }

    /**
     * Q-Insight: Gathers all metrics for the dashboard.
     * This is a read-only transactional method to ensure a consistent snapshot of the data is read
     * from the database. It orchestrates calls to different repositories and services.
     * @return A composite DTO containing all dashboard metrics.
     */
    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboardMetrics() {
        Optional<com.institucion.ticketero.module_workday.domain.Workday> activeWorkday = workdayService.getActiveWorkday();
        long totalTickets = activeWorkday.map(workday -> ticketRepository.countByCreatedAtAfter(workday.getStartTime())).orElse(0L);
        
        Map<String, Long> ticketsByStatus = Arrays.stream(TicketStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        ticketRepository::countByStatus
                ));

        List<QueueStatusResponse> queueDetails = queueService.getAllQueueStatus(activeWorkday.map(com.institucion.ticketero.module_workday.domain.Workday::getStartTime));
        List<ExecutiveStatusResponse> executiveDetails = executiveRepository.findAll().stream()
                .map(this::mapToExecutiveStatusResponse)
                .collect(Collectors.toList());

        return new DashboardMetricsResponse(totalTickets, ticketsByStatus, queueDetails, executiveDetails, List.of(com.institucion.ticketero.module_queues.domain.AttentionType.values()));
    }

    private ExecutiveStatusResponse mapToExecutiveStatusResponse(Executive executive) {
        String currentTicketNumber = ticketRepository.findByExecutiveIdAndStatus(executive.getId(), TicketStatus.ATTENDING)
                .stream()
                .map(Ticket::getTicketNumber)
                .findFirst()
                .orElse(null);

        return new ExecutiveStatusResponse(
                executive.getId(),
                executive.getFullName(),
                executive.getModule(),
                executive.getStatus(),
                currentTicketNumber,
                executive.getSupportedAttentionTypes()
        );
    }
}
