package com.institucion.ticketero.module_tickets.application;

import com.institucion.ticketero.module_audit.application.AuditService;
import com.institucion.ticketero.module_audit.domain.AuditEvent;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ExecutiveRepository executiveRepository;
    private final NotificationService notificationService;
    private final QueueService queueService;
    private final WorkdayService workdayService;
    private final AuditService auditService;
    private static final int MAX_RETRIES = 5;

    public TicketService(TicketRepository ticketRepository, ExecutiveRepository executiveRepository,
                         NotificationService notificationService, QueueService queueService,
                         WorkdayService workdayService, AuditService auditService) {
        this.ticketRepository = ticketRepository;
        this.executiveRepository = executiveRepository;
        this.notificationService = notificationService;
        this.queueService = queueService;
        this.workdayService = workdayService;
        this.auditService = auditService;
    }

    @Transactional
    public CreateTicketResponse createTicket(CreateTicketRequest request) {
        Workday currentWorkday = workdayService.getCurrentActiveWorkday();

        Ticket ticket = new Ticket();
        ticket.setNationalId(request.nationalId());
        ticket.setTelefono(request.telefono());
        ticket.setBranchOffice(request.branchOffice());
        ticket.setAttentionType(request.attentionType());
        ticket.setStatus(TicketStatus.EN_ESPERA);
        ticket.setWorkday(currentWorkday);

        Ticket savedTicket = null;
        int retries = 0;

        while (savedTicket == null && retries < MAX_RETRIES) {
            try {
                String ticketNumber = generateTicketNumber(request.attentionType(), currentWorkday.getId());
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

        long position = ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore(
                savedTicket.getAttentionType(), savedTicket.getWorkday().getId(), TicketStatus.EN_ESPERA, savedTicket.getCreatedAt()) + 1;
        
        ticket.setPositionInQueue((int) position);

        long estimatedWaitTime = queueService.calculateAverageWaitTime(savedTicket.getAttentionType(), position);
        ticket.setEstimatedWaitMinutes((int) estimatedWaitTime);
        
        savedTicket = ticketRepository.save(ticket);

        notificationService.sendTicketConfirmation(savedTicket, (int) position, estimatedWaitTime);
        auditService.recordEvent(AuditEvent.TICKET_CREADO, "SYSTEM", "TICKET", savedTicket.getId(), "Ticket created with number: " + savedTicket.getTicketNumber());

        return new CreateTicketResponse(savedTicket.getCodigoReferencia(), savedTicket.getTicketNumber(), (int) position, estimatedWaitTime, savedTicket.getAttentionType());
    }

    public TicketStatusResponse getTicketStatusByCodigoReferencia(UUID codigoReferencia) {
        Ticket ticket = ticketRepository.findByCodigoReferencia(codigoReferencia)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with reference code: " + codigoReferencia));

        long position = 0;
        long estimatedWaitTime = 0;

        if (ticket.getStatus() == TicketStatus.EN_ESPERA) {
            position = ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore(
                    ticket.getAttentionType(), ticket.getWorkday().getId(), TicketStatus.EN_ESPERA, ticket.getCreatedAt()) + 1;
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

    public TicketStatusResponse getTicketStatus(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));

        long position = 0;
        long estimatedWaitTime = 0;

        if (ticket.getStatus() == TicketStatus.EN_ESPERA) {
            position = ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore(
                    ticket.getAttentionType(), ticket.getWorkday().getId(), TicketStatus.EN_ESPERA, ticket.getCreatedAt()) + 1;
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
    
    private String generateTicketNumber(com.institucion.ticketero.module_queues.domain.AttentionType attentionType, Long workdayId) {
        String prefix = attentionType.getPrefix();
        
        long sequenceNumber = ticketRepository.countByAttentionTypeAndWorkdayId(attentionType, workdayId) + 1;
        
        return prefix + "-" + sequenceNumber;
    }

    @Transactional
    public void assignNextAvailableTicket() {
        for (com.institucion.ticketero.module_queues.domain.AttentionType type : Arrays.asList(com.institucion.ticketero.module_queues.domain.AttentionType.values())) {
            Optional<Executive> executiveOpt = executiveRepository.findFirstByStatusAndSupportedAttentionTypesContainingOrderByLastStatusChangeAsc(ExecutiveStatus.AVAILABLE, type);
            if (executiveOpt.isPresent()) {
                Optional<Ticket> ticketOpt = ticketRepository.findFirstByAttentionTypeAndStatusOrderByCreatedAtAsc(type, TicketStatus.EN_ESPERA);
                if (ticketOpt.isPresent()) {
                    Executive executive = executiveOpt.get();
                    Ticket ticket = ticketOpt.get();
                    executive.setStatus(ExecutiveStatus.BUSY);
                    executive.setLastStatusChange(LocalDateTime.now());
                    
                    ticket.setExecutive(executive);
                    ticket.setStatus(TicketStatus.ATENDIENDO);
                    ticket.setAttendedAt(LocalDateTime.now());

                    executiveRepository.save(executive);
                    ticketRepository.save(ticket);

                    notificationService.sendTurnActiveAlert(ticket);
                    auditService.recordEvent(AuditEvent.TICKET_ASIGNADO, "SYSTEM", "TICKET", ticket.getId(), "Ticket " + ticket.getTicketNumber() + " assigned to executive " + executive.getFullName());
                    return;
                }
            }
        }
    }

    @Transactional
    public void closeCurrentTicketForExecutive(Long executiveId) {
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new com.institucion.ticketero.common.exceptions.ResourceNotFoundException("Executive not found with ID: " + executiveId));

        if (executive.getStatus() == ExecutiveStatus.AVAILABLE) {
            return;
        }

        ticketRepository.findByExecutiveIdAndStatus(executive.getId(), TicketStatus.ATENDIENDO)
                .stream().findFirst()
                .ifPresent(ticket -> {
                    ticket.setStatus(TicketStatus.COMPLETADO);
                    ticket.setClosedAt(LocalDateTime.now());
                    ticketRepository.save(ticket);
                    auditService.recordEvent(AuditEvent.TICKET_COMPLETADO, executive.getFullName(), "TICKET", ticket.getId(), "Ticket " + ticket.getTicketNumber() + " completed by executive " + executive.getFullName());
                });

        executive.setStatus(ExecutiveStatus.AVAILABLE);
        executive.setLastStatusChange(LocalDateTime.now());
        executiveRepository.save(executive);
    }
}
