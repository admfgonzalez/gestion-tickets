package com.institucion.ticketero.module_tickets.application;

import com.institucion.ticketero.module_notifications.application.NotificationService;
import com.institucion.ticketero.module_queues.application.QueueService;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.api.CreateTicketRequest;
import com.institucion.ticketero.module_tickets.api.CreateTicketResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private QueueService queueService;

    @Mock
    private WorkdayService workdayService; // Mock WorkdayService

    @InjectMocks
    private TicketService ticketService;

    @Test
    void testCreateTicket() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678-9", AttentionType.CAJA, "12345");

        Workday mockWorkday = new Workday();
        mockWorkday.setId(UUID.randomUUID());
        mockWorkday.setStartTime(LocalDateTime.now().minusHours(1));
        when(workdayService.getCurrentActiveWorkday()).thenReturn(mockWorkday);

        Ticket savedTicket = new Ticket();
        savedTicket.setId(UUID.randomUUID());
        savedTicket.setTicketNumber("CA-1");
        savedTicket.setAttentionType(AttentionType.CAJA);
        savedTicket.setStatus(TicketStatus.PENDING);
        savedTicket.setCreatedAt(LocalDateTime.now());
        savedTicket.setWorkday(mockWorkday); // Set the mocked workday on the ticket

        // Mock for generateTicketNumber
        when(ticketRepository.countByAttentionTypeAndWorkdayId(any(AttentionType.class), any(UUID.class))).thenReturn(0L);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        // Mock for position calculation
        when(ticketRepository.countByAttentionTypeAndWorkdayIdAndStatusAndCreatedAtBefore(
                any(AttentionType.class), any(UUID.class), any(TicketStatus.class), any(LocalDateTime.class))).thenReturn(4L);
        when(queueService.calculateAverageWaitTime(any(), any(Long.class))).thenReturn(20L);

        // When
        CreateTicketResponse response = ticketService.createTicket(request);

        // Then
        assertEquals("CA-1", response.ticketNumber());
        assertEquals(5, response.queuePosition());
        assertEquals(20, response.estimatedWaitTimeMinutes());

        verify(notificationService).sendTicketConfirmation(any(Ticket.class), any(Integer.class), any(Long.class));
    }
}
