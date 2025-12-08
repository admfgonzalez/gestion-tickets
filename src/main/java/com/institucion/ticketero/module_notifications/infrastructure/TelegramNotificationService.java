package com.institucion.ticketero.module_notifications.infrastructure;

import com.institucion.ticketero.module_notifications.application.NotificationService;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Q-Insight: Infrastructure Service for Telegram Notifications.
 * This class implements the NotificationService interface using the Telegram Bot API.
 * It handles the formatting of messages and communication with the Telegram service.
 * Methods are marked @Async to prevent blocking the main application thread.
 */
@Service
public class TelegramNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final TelegramBot bot;
    private final String botUsername;

    public TelegramNotificationService(@Value("${telegram.bot.token}") String botToken,
                                       @Value("${telegram.bot.username}") String botUsername) {
        this.bot = new TelegramBot(botToken);
        this.botUsername = botUsername;
    }

    @PostConstruct
    public void init() {
        // Set up the listener to receive updates from Telegram
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                logger.debug("Received Telegram update: {}", update);
                // Process only new messages
                if (update.message() != null && update.message().text() != null) {
                    Long chatId = update.message().chat().id();
                    String messageText = update.message().text();

                    if (messageText.equals("/start")) {
                        String response = "Hola! Tu ID de chat es: `" + chatId + "`\n" +
                                "Por favor, copia este ID y pégalo en el campo 'ID de Chat de Telegram' en el formulario.";
                        sendMessage(String.valueOf(chatId), response);
                    }
                } else if (update.myChatMember() != null && update.myChatMember().newChatMember().status().equals("member")) {
                    // Handle bot being added to a chat (e.g., a new user starting a private chat with the bot)
                    Long chatId = update.myChatMember().chat().id();
                    String response = "¡Gracias por iniciar una conversación! Tu ID de chat es: `" + chatId + "`\n" +
                            "Por favor, copia este ID y pégalo en el campo 'ID de Chat de Telegram' en el formulario.";
                    sendMessage(String.valueOf(chatId), response);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Async
    @Override
    public void sendTicketConfirmation(Ticket ticket, int positionInQueue, long estimatedWaitTimeMinutes) {
        if (ticket.getTelegramChatId() == null || ticket.getTelegramChatId().isBlank()) {
            logger.warn("Cannot send confirmation for ticket {}: no Telegram chat ID.", ticket.getTicketNumber());
            return;
        }

        String message = String.format(
                "✅ *Ticket Confirmado* ✅\n\n" +
                "Hola! Tu ticket ha sido creado con éxito.\n\n" +
                "Número de Ticket: *%s*\n" +
                "Tipo de Atención: *%s*\n" +
                "Posición en Fila: *%d*\n" +
                "Tiempo de Espera Estimado: *%d minutos*\n\n" +
                "Te notificaremos cuando tu turno esté cerca.",
                ticket.getTicketNumber(),
                ticket.getAttentionType().name(),
                positionInQueue,
                estimatedWaitTimeMinutes
        );

        sendMessage(ticket.getTelegramChatId(), message);
    }

    @Async
    @Override
    public void sendPreArrivalAlert(Ticket ticket) {
        if (ticket.getTelegramChatId() == null || ticket.getTelegramChatId().isBlank()) {
            return; // Fail silently
        }

        String message = String.format(
                "⏳ *Tu Turno está Cerca!* ⏳\n\n" +
                "Por favor, acércate a la sucursal. Quedan pocas personas antes que tú.\n\n" +
                "Número de Ticket: *%s*",
                ticket.getTicketNumber()
        );

        sendMessage(ticket.getTelegramChatId(), message);
    }

    @Async
    @Override
    public void sendTurnActiveAlert(Ticket ticket) {
        if (ticket.getTelegramChatId() == null || ticket.getTelegramChatId().isBlank() || ticket.getExecutive() == null) {
            return; // Fail silently
        }

        String message = String.format(
                "‼️ *Es Tu Turno!* ‼️\n\n" +
                "Por favor, dirígete al módulo de atención indicado.\n\n" +
                "Número de Ticket: *%s*\n" +
                "Asesor: *%s*\n" +
                "Módulo: *%s*",
                ticket.getTicketNumber(),
                ticket.getExecutive().getFullName(),
                ticket.getExecutive().getModule()
        );

        sendMessage(ticket.getTelegramChatId(), message);
    }

    private void sendMessage(String chatId, String text) {
        try {
            SendMessage request = new SendMessage(chatId, text).parseMode(com.pengrad.telegrambot.model.request.ParseMode.Markdown);
            bot.execute(request);
            logger.info("Successfully sent Telegram message to chatId: {}", chatId);
        } catch (Exception e) {
            // In a real app, add retry logic here (RNF-004)
            logger.error("Failed to send Telegram message to chatId: {}. Reason: {}", chatId, e.getMessage());
        }
    }
}
