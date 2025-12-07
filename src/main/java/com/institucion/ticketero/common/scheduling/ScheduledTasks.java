package com.institucion.ticketero.common.scheduling;

import com.institucion.ticketero.module_notifications.application.NotificationService;
import com.institucion.ticketero.module_tickets.application.TicketService;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Q-Insight: Background Scheduled Tasks.
 * This component handles periodic background processing for the application.
 * It uses Spring's @Scheduled annotation to trigger methods at fixed intervals.
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final int PRE_ARRIVAL_POSITION = 3;

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;

    // A set to keep track of tickets that have already been notified to avoid sending duplicate alerts.
    private final Set<UUID> notifiedTickets = ConcurrentHashMap.newKeySet();

    public ScheduledTasks(TicketService ticketService, TicketRepository ticketRepository, NotificationService notificationService) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
    }

    /**
     * Q-Insight: Automatic Ticket Assignment Job.
     * This scheduled task runs every 10 seconds and attempts to assign a pending ticket to an available executive.
     * This implements the core automatic assignment logic (RF-004). The rate can be configured.
     */
    @Scheduled(fixedRate = 10000) // 10 seconds
    public void assignTickets() {
        logger.debug("Running scheduled job: assignNextAvailableTicket");
        ticketService.assignNextAvailableTicket();
    }

    /**
     * Q-Insight: Pre-Arrival Notification Job.
     * This task runs every 15 seconds to check for customers who are nearing the front of the line
     * and sends them a pre-arrival notification (RF-002, Message 2).
     */
    @Scheduled(fixedRate = 15000) // 15 seconds
    public void checkForPreArrivalAlerts() {
        logger.debug("Running scheduled job: checkForPreArrivalAlerts");
        ticketRepository.findTicketsAtPositionInQueue(PRE_ARRIVAL_POSITION).forEach(ticket -> {
            if (!notifiedTickets.contains(ticket.getId())) {
                notificationService.sendPreArrivalAlert(ticket);
                notifiedTickets.add(ticket.getId());
                logger.info("Sent pre-arrival alert for ticket {}", ticket.getTicketNumber());
            }
        });
    }
}
