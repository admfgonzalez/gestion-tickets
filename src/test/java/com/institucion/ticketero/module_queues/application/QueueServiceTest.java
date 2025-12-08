package com.institucion.ticketero.module_queues.application;

import com.institucion.ticketero.module_queues.api.QueueStatusResponse;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private QueueService queueService;

    @Test
    void getAllQueueStatus_shouldReturnStatusForAllQueues() {
        LocalDateTime startTime = LocalDateTime.now();
        when(ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(eq(AttentionType.CAJA), eq(TicketStatus.PENDING), any())).thenReturn(5L);
        when(ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(eq(AttentionType.PERSONAL_BANKER), eq(TicketStatus.PENDING), any())).thenReturn(2L);
        when(ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(eq(AttentionType.EMPRESAS), eq(TicketStatus.PENDING), any())).thenReturn(0L);
        when(ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(eq(AttentionType.GERENCIA), eq(TicketStatus.PENDING), any())).thenReturn(1L);

        List<QueueStatusResponse> responses = queueService.getAllQueueStatus(Optional.of(startTime));

        assertEquals(AttentionType.values().length, responses.size());
    }

    @Test
    void getQueueStatus_shouldReturnCorrectStatus() {
        LocalDateTime startTime = LocalDateTime.now();
        AttentionType attentionType = AttentionType.CAJA;
        long waitingCustomers = 5;
        long expectedWaitTime = waitingCustomers * attentionType.getAverageServiceTimeMinutes();

        when(ticketRepository.countByAttentionTypeAndStatusAndCreatedAtBefore(eq(attentionType), eq(TicketStatus.PENDING), any())).thenReturn(waitingCustomers);
        
        QueueStatusResponse response = queueService.getQueueStatus(attentionType, Optional.of(startTime));
        
        assertNotNull(response);
        assertEquals(attentionType, response.attentionType());
        assertEquals(waitingCustomers, response.waitingCustomers());
        assertEquals(expectedWaitTime, response.averageWaitTimeMinutes());
    }

    @Test
    void calculateAverageWaitTime_withZeroCustomers_shouldReturnZero() {
        long waitTime = queueService.calculateAverageWaitTime(AttentionType.CAJA, 0);
        assertEquals(0, waitTime);
    }

    @Test
    void calculateAverageWaitTime_withCustomers_shouldReturnCorrectTime() {
        long waitingCustomers = 3;
        AttentionType attentionType = AttentionType.PERSONAL_BANKER;
        long expectedWaitTime = waitingCustomers * attentionType.getAverageServiceTimeMinutes();
        
        long waitTime = queueService.calculateAverageWaitTime(attentionType, waitingCustomers);
        
        assertEquals(expectedWaitTime, waitTime);
    }
}
