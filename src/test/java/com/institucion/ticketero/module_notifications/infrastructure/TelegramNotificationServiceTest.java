package com.institucion.ticketero.module_notifications.infrastructure;

import com.institucion.ticketero.module_executives.domain.Executive;
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

    // We use @InjectMocks to let Mockito create the service and inject the bot mock.
    // However, since the service uses @Value annotations, we must manually set these fields after construction.
    private TelegramNotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Manually instantiate the service to handle constructor injection of @Value fields
        notificationService = new TelegramNotificationService("fake-token", "fake-bot-username");
        // Use Spring's ReflectionTestUtils to replace the real bot instance with our mock
        ReflectionTestUtils.setField(notificationService, "bot", bot);
    }

    @Test
    void whenSendTicketConfirmation_thenBotExecuteIsCalled() {
        // Given
        Executive executive = new Executive(); // Not used in this message, but part of the object
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("C-123");
        ticket.setCustomerId("RUT123");
        ticket.setAttentionType(AttentionType.CAJA);
        ticket.setTelegramChatId("chat123");
        ticket.setExecutive(executive);

        int positionInQueue = 5;
        long estimatedWaitTime = 20;

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        // When
        notificationService.sendTicketConfirmation(ticket, positionInQueue, estimatedWaitTime);

        // Then
        verify(bot, times(1)).execute(captor.capture());
        String sentMessage = (String) captor.getValue().getParameters().get("text");

        assertTrue(sentMessage.contains("Ticket Confirmado"));
        assertTrue(sentMessage.contains("C-123"));
        assertTrue(sentMessage.contains("Posición en Fila: *5*"));
        assertTrue(sentMessage.contains("Tiempo de Espera Estimado: *20 minutos*"));
    }

    @Test
    void whenSendPreArrivalAlert_thenBotExecuteIsCalled() {
        // Given
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("E-456");
        ticket.setCustomerId("RUT456");
        ticket.setAttentionType(AttentionType.EMPRESAS);
        ticket.setTelegramChatId("chat456");

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        // When
        notificationService.sendPreArrivalAlert(ticket);

        // Then
        verify(bot, times(1)).execute(captor.capture());
        String sentMessage = (String) captor.getValue().getParameters().get("text");
        assertTrue(sentMessage.contains("Tu Turno está Cerca"));
        assertTrue(sentMessage.contains("E-456"));
    }

    @Test
    void whenSendTurnActiveAlert_thenBotExecuteIsCalled() {
        // Given
        Executive executive = new Executive();
        ReflectionTestUtils.setField(executive, "fullName", "John Doe");
        ReflectionTestUtils.setField(executive, "module", "Modulo Test");

        Ticket ticket = new Ticket();
        ticket.setTicketNumber("G-789");
        ticket.setCustomerId("RUT789");
        ticket.setAttentionType(AttentionType.GERENCIA);
        ticket.setTelegramChatId("chat789");
        ticket.setExecutive(executive);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);

        // When
        notificationService.sendTurnActiveAlert(ticket);

        // Then
        verify(bot, times(1)).execute(captor.capture());
        String sentMessage = (String) captor.getValue().getParameters().get("text");
        assertTrue(sentMessage.contains("Es Tu Turno"));
        assertTrue(sentMessage.contains("G-789"));
        assertTrue(sentMessage.contains("Asesor: *John Doe*"));
        assertTrue(sentMessage.contains("Módulo: *Modulo Test*"));
    }

    @Test
    void whenChatIdIsMissing_thenBotIsNotCalled() {
        // Given a ticket with a null chat ID
        Ticket ticket = new Ticket();
        ticket.setTicketNumber("C-000");
        ticket.setCustomerId("RUT000");
        ticket.setAttentionType(AttentionType.CAJA);
        ticket.setTelegramChatId(null);

        // When
        notificationService.sendTicketConfirmation(ticket, 1, 10);
        notificationService.sendPreArrivalAlert(ticket);
        notificationService.sendTurnActiveAlert(ticket);

        // Then
        verify(bot, never()).execute(any(SendMessage.class));
    }
}
