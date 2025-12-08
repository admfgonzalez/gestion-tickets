package com.institucion.ticketero.module_tickets.application;

import com.institucion.ticketero.module_notifications.application.NotificationService;
import com.institucion.ticketero.module_queues.application.QueueService;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.api.CreateTicketRequest;
import com.institucion.ticketero.module_tickets.api.CreateTicketResponse;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
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

    @InjectMocks
    private TicketService ticketService;
    
    @BeforeEach
    void setUp() {
        // Reset the counter before each test to ensure isolation
        ticketService.resetTicketCounter();
    }

    @Test
    void testCreateTicket() {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678-9", AttentionType.CAJA, "12345");

        Ticket savedTicket = new Ticket();
        savedTicket.setId(UUID.randomUUID());
        savedTicket.setTicketNumber("CA-1");
        savedTicket.setAttentionType(AttentionType.CAJA);
        savedTicket.setStatus(TicketStatus.PENDING);
        savedTicket.setCreatedAt(LocalDateTime.now());

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);
        when(ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(any(), any(), any())).thenReturn(4L);
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
