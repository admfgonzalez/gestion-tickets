package com.institucion.ticketero.module_notifications.infrastructure;

import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_messages.infrastructure.MessageRepository;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramNotificationServiceTest {

    @Mock
    private TelegramBot bot;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private TelegramNotificationService notificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationService, "bot", bot);
        ReflectionTestUtils.setField(notificationService, "botUsername", "fake-bot-username");
        ReflectionTestUtils.setField(notificationService, "welcomeMessage", "Welcome!");
    }

    @Test
    void whenSendTicketConfirmation_thenMessageIsSaved() {
        // Given
        Executive executive = new Executive();
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("C-123");
        ticket.setNationalId("RUT123");
        ticket.setAttentionType(AttentionType.CAJA);
        ticket.setTelefono("chat123");
        ticket.setExecutive(executive);

        int positionInQueue = 5;
        long estimatedWaitTime = 20;

        // When
        notificationService.sendTicketConfirmation(ticket, positionInQueue, estimatedWaitTime);

        // Then
        verify(messageRepository, times(1)).save(any());
    }

    @Test
    void whenSendPreArrivalAlert_thenMessageIsSaved() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("E-456");
        ticket.setNationalId("RUT456");
        ticket.setAttentionType(AttentionType.EMPRESAS);
        ticket.setTelefono("chat456");

        // When
        notificationService.sendPreArrivalAlert(ticket);

        // Then
        verify(messageRepository, times(1)).save(any());
    }

    @Test
    void whenSendTurnActiveAlert_thenMessageIsSaved() {
        // Given
        Executive executive = new Executive();
        ReflectionTestUtils.setField(executive, "fullName", "John Doe");
        ReflectionTestUtils.setField(executive, "module", "Modulo Test");

        Ticket ticket = new Ticket();
        ticket.setTicketNumber("G-789");
        ticket.setNationalId("RUT789");
        ticket.setAttentionType(AttentionType.GERENCIA);
        ticket.setTelefono("chat789");
        ticket.setExecutive(executive);

        // When
        notificationService.sendTurnActiveAlert(ticket);

        // Then
        verify(messageRepository, times(1)).save(any());
    }

    @Test
    void whenChatIdIsMissing_thenBotIsNotCalled() {
        // Given a ticket with a null chat ID
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("C-000");
        ticket.setNationalId("RUT000");
        ticket.setAttentionType(AttentionType.CAJA);
        ticket.setTelefono(null);

        // When
        notificationService.sendTicketConfirmation(ticket, 1, 10);
        notificationService.sendPreArrivalAlert(ticket);
        notificationService.sendTurnActiveAlert(ticket);

        // Then
        verify(messageRepository, never()).save(any());
    }
}
