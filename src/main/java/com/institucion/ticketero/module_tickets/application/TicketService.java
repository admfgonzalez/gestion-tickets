package com.institucion.ticketero.module_tickets.application;

import com.institucion.ticketero.common.exceptions.ResourceNotFoundException;
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
import com.institucion.ticketero.module_workday.application.WorkdayService;
import com.institucion.ticketero.module_workday.domain.Workday;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

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
    private final WorkdayService workdayService; // Inject WorkdayService
    private static final int MAX_RETRIES = 5;

    public TicketService(TicketRepository ticketRepository, ExecutiveRepository executiveRepository,
                         NotificationService notificationService, QueueService queueService,
                         WorkdayService workdayService) { // Add WorkdayService to constructor
        this.ticketRepository = ticketRepository;
        this.executiveRepository = executiveRepository;
        this.notificationService = notificationService;
        this.queueService = queueService;
        this.workdayService = workdayService;
    }

    /**
     * Q-Insight: Creates a new ticket.
     * This method implements the RF-001 use case. It's marked @Transactional to ensure all database
     * operations within it either succeed or fail together, maintaining data consistency.
     * It now includes a retry mechanism to handle race conditions during ticket number generation,
     * considering the active workday for uniqueness.
     * @param request The DTO containing the new ticket data.
     * @return A DTO with the details of the created ticket.
     */
    @Transactional
    public CreateTicketResponse createTicket(CreateTicketRequest request) {
        Workday currentWorkday = workdayService.getCurrentActiveWorkday();

        Ticket ticket = new Ticket();
        ticket.setCustomerId(request.customerId());
        ticket.setTelegramChatId(request.telegramChatId());
        ticket.setAttentionType(request.attentionType());
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setWorkday(currentWorkday); // Associate ticket with the current workday

        Ticket savedTicket = null;
        int retries = 0;

        while (savedTicket == null && retries < MAX_RETRIES) {
            try {
                String ticketNumber = generateTicketNumber(request.attentionType(), currentWorkday.getId()); // Pass workday ID
                ticket.setTicketNumber(ticketNumber);
                
                savedTicket = ticketRepository.saveAndFlush(ticket);

            } catch (DataIntegrityViolationException e) {
                retries++;
                try {
                    Thread.sleep(50 + new Random().nextInt(50));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Ticket creation interrupted.", ie);
                }
            }
        }

        if (savedTicket == null) {
            throw new RuntimeException("Failed to create a unique ticket after " + MAX_RETRIES + " retries.");
        }

        long position = ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore( // Use new method
                savedTicket.getAttentionType(), savedTicket.getWorkday().getId(), TicketStatus.PENDING, savedTicket.getCreatedAt()) + 1;

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
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));

        long position = 0;
        long estimatedWaitTime = 0;

        if (ticket.getStatus() == TicketStatus.PENDING) {
            position = ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore( // Use new method
                    ticket.getAttentionType(), ticket.getWorkday().getId(), TicketStatus.PENDING, ticket.getCreatedAt()) + 1;
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
     * Q-Insight: Generates a unique, human-readable ticket number that is unique per attention type and workday.
     * Example: CA-1, CA-2, PB-1, PB-2 (within a specific workday). The counters reset each workday.
     * This logic is robust against application restarts and multiple instances.
     * @param attentionType The type of attention for the ticket.
     * @param workdayId The ID of the current active workday.
     * @return The generated ticket number string.
     */
    private String generateTicketNumber(com.institucion.ticketero.module_queues.domain.AttentionType attentionType, UUID workdayId) {
        String prefix = switch (attentionType) {
            case CAJA -> "CA";
            case PERSONAL_BANKER -> "PB";
            case EMPRESAS -> "EM";
            case GERENCIA -> "GE";
        };
        
        // Get the count of tickets of this type created for the current workday and add 1.
        long sequenceNumber = ticketRepository.countByAttentionTypeAndWorkdayId(attentionType, workdayId) + 1;
        
        return prefix + "-" + sequenceNumber;
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

    /**
     * Closes the currently active ticket for a specific executive.
     * This is a manual action triggered from the supervisor dashboard.
     * @param executiveId The ID of the executive finishing the attention.
     */
    @Transactional
    public void closeCurrentTicketForExecutive(UUID executiveId) {
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new com.institucion.ticketero.common.exceptions.ResourceNotFoundException("Executive not found with ID: " + executiveId));

        if (executive.getStatus() == ExecutiveStatus.AVAILABLE) {
            // Nothing to do if the executive is already available
            return;
        }

        // Find the ticket this executive is currently attending
        ticketRepository.findByExecutiveIdAndStatus(executive.getId(), TicketStatus.ATTENDING)
                .stream().findFirst()
                .ifPresent(ticket -> {
                    ticket.setStatus(TicketStatus.CLOSED);
                    ticket.setClosedAt(LocalDateTime.now());
                    ticketRepository.save(ticket);
                });

        // Free up the executive
        executive.setStatus(ExecutiveStatus.AVAILABLE);
        executive.setLastStatusChange(LocalDateTime.now());
        executiveRepository.save(executive);
    }
}
