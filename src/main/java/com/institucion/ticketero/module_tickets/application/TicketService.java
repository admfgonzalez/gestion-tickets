package com.institucion.ticketero.module_tickets.application;

import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_executives.infrastructure.ExecutiveRepository;
import com.institucion.ticketero.module_notifications.application.NotificationService;
import com.institucion.ticketero.module_queues.application.QueueService;
import com.institucion.ticketero.module_tickets.api.CreateTicketRequest;
import com.institucion.ticketero.module_tickets.api.CreateTicketResponse;
import com.institucion.ticketero.module_tickets.api.TicketStatusResponse;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Q-Insight: Application Service for Tickets.
 * This service orchestrates all business logic related to the ticket lifecycle.
 * It's the heart of the application, coordinating repositories, other services, and domain entities.
 */
@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ExecutiveRepository executiveRepository;
    private final NotificationService notificationService;
    private final QueueService queueService;
    private final AtomicLong ticketCounter = new AtomicLong(1); // Initial sequence number

    public TicketService(TicketRepository ticketRepository, ExecutiveRepository executiveRepository, NotificationService notificationService, QueueService queueService) {
        this.ticketRepository = ticketRepository;
        this.executiveRepository = executiveRepository;
        this.notificationService = notificationService;
        this.queueService = queueService;
    }

    /**
     * Q-Insight: Creates a new ticket.
     * This method implements the RF-001 use case. It's marked @Transactional to ensure all database
     * operations within it either succeed or fail together, maintaining data consistency.
     * @param request The DTO containing the new ticket data.
     * @return A DTO with the details of the created ticket.
     */
    @Transactional
    public CreateTicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(request.customerId());
        ticket.setTelegramChatId(request.telegramChatId());
        ticket.setAttentionType(request.attentionType());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setTicketNumber(generateTicketNumber(request.attentionType()));

        Ticket savedTicket = ticketRepository.save(ticket);

        long position = ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(
                savedTicket.getAttentionType(), TicketStatus.PENDING, savedTicket.getCreatedAt()) + 1;

        long estimatedWaitTime = queueService.calculateAverageWaitTime(savedTicket.getAttentionType(), position);

        notificationService.sendTicketConfirmation(savedTicket, (int) position, estimatedWaitTime);

        return new CreateTicketResponse(savedTicket.getTicketNumber(), (int) position, estimatedWaitTime);
    }

    /**
     * Q-Insight: Gets the status of a specific ticket.
     * Implements the RF-006 use case.
     * @param ticketNumber The ticket number to query.
     * @return A DTO with the detailed status of the ticket.
     */
    public TicketStatusResponse getTicketStatus(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new com.institucion.ticketero.common.exceptions.ResourceNotFoundException("Ticket not found with number: " + ticketNumber));

        long position = 0;
        long estimatedWaitTime = 0;

        if (ticket.getStatus() == TicketStatus.PENDING) {
            position = ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(
                    ticket.getAttentionType(), TicketStatus.PENDING, ticket.getCreatedAt()) + 1;
            estimatedWaitTime = queueService.calculateAverageWaitTime(ticket.getAttentionType(), position);
        }

        Executive executive = ticket.getExecutive();
        String executiveName = executive != null ? executive.getFullName() : null;
        String executiveModule = executive != null ? executive.getModule() : null;

        return new TicketStatusResponse(
                ticket.getTicketNumber(),
                ticket.getStatus(),
                (int) position,
                estimatedWaitTime,
                executiveName,
                executiveModule
        );
    }
    
    /**
     * Q-Insight: Generates a unique, human-readable ticket number.
     * Example: CAJA -> CA-101, PERSONAL_BANKER -> PB-102.
     * @param attentionType The type of attention for the ticket.
     * @return The generated ticket number string.
     */
    private String generateTicketNumber(com.institucion.ticketero.module_queues.domain.AttentionType attentionType) {
        String prefix = switch (attentionType) {
            case CAJA -> "CA";
            case PERSONAL_BANKER -> "PB";
            case EMPRESAS -> "EM";
            case GERENCIA -> "GE";
        };
        return prefix + "-" + ticketCounter.incrementAndGet();
    }

    /**
     * Q-Insight: Assigns the next pending ticket to an available executive.
     * This method implements the core logic of RF-004 (Automatic Assignment).
     * It should be called periodically by a scheduled task.
     * The logic is transactional to ensure atomicity.
     */
    @Transactional
    public void assignNextAvailableTicket() {
        // Iterate through queue types by priority
        Arrays.stream(com.institucion.ticketero.module_queues.domain.AttentionType.values())
                .sorted((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()))
                .forEach(type -> {
            executiveRepository.findFirstByStatusAndSupportedAttentionTypesContainingOrderByLastStatusChangeAsc(ExecutiveStatus.AVAILABLE, type)
                    .ifPresent(executive -> {
                        ticketRepository.findFirstByAttentionTypeAndStatusOrderByCreatedAtAsc(type, TicketStatus.PENDING)
                                .ifPresent(ticket -> {
                                    // Assign ticket to executive
                                    executive.setStatus(ExecutiveStatus.BUSY);
                                    executive.setLastStatusChange(LocalDateTime.now());
                                    
                                    ticket.setExecutive(executive);
                                    ticket.setStatus(TicketStatus.ATTENDING);
                                    ticket.setAttendedAt(LocalDateTime.now());

                                    executiveRepository.save(executive);
                                    ticketRepository.save(ticket);

                                    notificationService.sendTurnActiveAlert(ticket);
                                });
                    });
        });
    }
}
