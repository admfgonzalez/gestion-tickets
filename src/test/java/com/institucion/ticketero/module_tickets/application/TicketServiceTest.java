package com.institucion.ticketero.module_tickets.application;

import com.institucion.ticketero.module_audit.application.AuditService;
import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_executives.infrastructure.ExecutiveRepository;
import com.institucion.ticketero.module_notifications.application.NotificationService;
import com.institucion.ticketero.module_queues.application.QueueService;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.api.CreateTicketRequest;
import com.institucion.ticketero.module_tickets.api.CreateTicketResponse;
import com.institucion.ticketero.module_tickets.api.TicketStatusResponse;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import com.institucion.ticketero.module_workday.application.WorkdayService;
import com.institucion.ticketero.module_workday.domain.Workday;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ExecutiveRepository executiveRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private QueueService queueService;

    @Mock
    private WorkdayService workdayService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void testCreateTicket() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678-9", AttentionType.CAJA, "12345", "Main Branch");

        Workday mockWorkday = new Workday();
        mockWorkday.setId(1L);
        mockWorkday.setStartTime(LocalDateTime.now().minusHours(1));
        when(workdayService.getCurrentActiveWorkday()).thenReturn(mockWorkday);

        Ticket savedTicket = new Ticket();
        savedTicket.setId(1L);
        savedTicket.setCodigoReferencia(UUID.randomUUID());
        savedTicket.setTicketNumber("C-1");
        savedTicket.setAttentionType(AttentionType.CAJA);
        savedTicket.setStatus(TicketStatus.EN_ESPERA);
        savedTicket.setCreatedAt(LocalDateTime.now());
        savedTicket.setWorkday(mockWorkday);

        // Mock for generateTicketNumber
        when(ticketRepository.countByAttentionTypeAndWorkdayId(any(AttentionType.class), any(Long.class))).thenReturn(0L);

        when(ticketRepository.saveAndFlush(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        // Mock for position calculation
        when(ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore(
                any(AttentionType.class), any(Long.class), any(TicketStatus.class), any(LocalDateTime.class))).thenReturn(4L);
        when(queueService.calculateAverageWaitTime(any(), any(Long.class))).thenReturn(20L);

        // When
        CreateTicketResponse response = ticketService.createTicket(request);

        // Then
        assertEquals("C-1", response.ticketNumber());
        assertEquals(5, response.positionInQueue());
        assertEquals(20, response.estimatedWaitMinutes());

        verify(notificationService).sendTicketConfirmation(any(Ticket.class), any(Integer.class), any(Long.class));
        verify(auditService).recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    void testGetTicketStatus() {
        // Given
        Workday mockWorkday = new Workday();
        mockWorkday.setId(1L);
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("C-1");
        ticket.setStatus(TicketStatus.EN_ESPERA);
        ticket.setWorkday(mockWorkday);
        ticket.setAttentionType(AttentionType.CAJA);
        ticket.setCreatedAt(LocalDateTime.now());
        when(ticketRepository.findByTicketNumber("C-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore(
                any(AttentionType.class), any(Long.class), any(TicketStatus.class), any(LocalDateTime.class))).thenReturn(0L);
        when(queueService.calculateAverageWaitTime(any(), any(Long.class))).thenReturn(5L);

        // When
        var response = ticketService.getTicketStatus("C-1");

        // Then
        assertEquals("C-1", response.ticketNumber());
        assertEquals(TicketStatus.EN_ESPERA, response.status());
        assertEquals(1, response.queuePosition());
        assertEquals(5, response.estimatedWaitTimeMinutes());
    }

    @Test
    void testAssignNextAvailableTicket() {
        // Given
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setStatus(ExecutiveStatus.AVAILABLE);
        Workday mockWorkday = new Workday();
        mockWorkday.setId(1L);
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(TicketStatus.EN_ESPERA);
        ticket.setWorkday(mockWorkday);
        when(executiveRepository.findFirstByStatusAndSupportedAttentionTypesContainingOrderByLastStatusChangeAsc(any(), any())).thenReturn(Optional.of(executive));
        when(ticketRepository.findFirstByAttentionTypeAndStatusOrderByCreatedAtAsc(any(), any())).thenReturn(Optional.of(ticket));

        // When
        ticketService.assignNextAvailableTicket();

        // Then
        verify(executiveRepository, times(1)).save(any(Executive.class));
        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(notificationService, times(1)).sendTurnActiveAlert(any(Ticket.class));
        verify(auditService, times(1)).recordEvent(any(), any(), any(), any(), any());
    }

    @Test
    void testCloseCurrentTicketForExecutive() {
        // Given
        Executive executive = new Executive();
        executive.setId(1L);
        executive.setStatus(ExecutiveStatus.BUSY);
        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus(TicketStatus.ATENDIENDO);
        when(executiveRepository.findById(1L)).thenReturn(Optional.of(executive));
        when(ticketRepository.findByExecutiveIdAndStatus(1L, TicketStatus.ATENDIENDO)).thenReturn(java.util.List.of(ticket));

        // When
        ticketService.closeCurrentTicketForExecutive(1L);

        // Then
        verify(executiveRepository).save(any(Executive.class));
        verify(ticketRepository).save(any(Ticket.class));
        verify(auditService).recordEvent(any(), any(), any(), any(), any());
    }
}
