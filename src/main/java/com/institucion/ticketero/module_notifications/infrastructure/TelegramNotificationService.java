
package com.institucion.ticketero.module_notifications.infrastructure;

import com.institucion.ticketero.module_messages.domain.Message;
import com.institucion.ticketero.module_messages.domain.MessageStatus;
import com.institucion.ticketero.module_messages.domain.MessageTemplate;
import com.institucion.ticketero.module_messages.infrastructure.MessageRepository;
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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TelegramNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramNotificationService.class);

    private final TelegramBot bot;
    private final String botUsername;
    private final String welcomeMessage;
    private final MessageRepository messageRepository;

    public TelegramNotificationService(@Value("${telegram.bot.token}") String botToken,
                                       @Value("${telegram.bot.username}") String botUsername,
                                       @Value("${telegram.bot.welcome.message}") String welcomeMessage,
                                       MessageRepository messageRepository) {
        this.bot = new TelegramBot(botToken);
        this.botUsername = botUsername;
        this.welcomeMessage = welcomeMessage;
        this.messageRepository = messageRepository;
    }

    @PostConstruct
    public void init() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                logger.debug("Received Telegram update: {}", update);
                if (update.message() != null && update.message().text() != null) {
                    logger.info("Received message: " + update.message().text());
                } else if (update.myChatMember() != null && "member".equals(update.myChatMember().newChatMember().status().name())) {
                    logger.info("Bot was added to a chat: " + update.myChatMember().chat().id());
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
        if (ticket.getTelefono() == null || ticket.getTelefono().isBlank()) {
            logger.warn("Cannot send confirmation for ticket {}: no Telegram chat ID.", ticket.getTicketNumber());
            return;
        }

        String content = String.format(MessageTemplate.TOTEM_TICKET_CREADO.getTemplate(),
                ticket.getTicketNumber(),
                positionInQueue,
                estimatedWaitTimeMinutes);

        Message message = new Message();
        message.setTicket(ticket);
        message.setPlantilla(MessageTemplate.TOTEM_TICKET_CREADO);
        message.setEstadoEnvio(MessageStatus.PENDIENTE);
        message.setFechaProgramada(LocalDateTime.now());
        message.setChatId(ticket.getTelefono());
        message.setContent(content);
        messageRepository.save(message);
    }

    @Async
    @Override
    public void sendPreArrivalAlert(Ticket ticket) {
        if (ticket.getTelefono() == null || ticket.getTelefono().isBlank()) {
            return;
        }

        String content = String.format(MessageTemplate.TOTEM_PROXIMO_TURNO.getTemplate(), ticket.getTicketNumber());

        Message message = new Message();
        message.setTicket(ticket);
        message.setPlantilla(MessageTemplate.TOTEM_PROXIMO_TURNO);
        message.setEstadoEnvio(MessageStatus.PENDIENTE);
        message.setFechaProgramada(LocalDateTime.now());
        message.setChatId(ticket.getTelefono());
        message.setContent(content);
        messageRepository.save(message);
    }

    @Async
    @Override
    public void sendTurnActiveAlert(Ticket ticket) {
        if (ticket.getTelefono() == null || ticket.getTelefono().isBlank() || ticket.getExecutive() == null) {
            return;
        }

        String content = String.format(MessageTemplate.TOTEM_ES_TU_TURNO.getTemplate(),
                ticket.getTicketNumber(),
                ticket.getExecutive().getModule(),
                ticket.getExecutive().getFullName());

        Message message = new Message();
        message.setTicket(ticket);
        message.setPlantilla(MessageTemplate.TOTEM_ES_TU_TURNO);
        message.setEstadoEnvio(MessageStatus.PENDIENTE);
        message.setFechaProgramada(LocalDateTime.now());
        message.setChatId(ticket.getTelefono());
        message.setContent(content);
        messageRepository.save(message);
    }

    @Scheduled(fixedRate = 60000) // every 60 seconds
    public void processPendingMessages() {
        List<Message> pendingMessages = messageRepository.findByEstadoEnvioAndFechaProgramadaBefore(MessageStatus.PENDIENTE, LocalDateTime.now());
        for (Message message : pendingMessages) {
            sendMessage(message);
        }
    }

    @Retryable(value = Exception.class, maxAttempts = 4, backoff = @Backoff(delay = 30000, multiplier = 2))
    public void sendMessage(Message message) {
        message.setIntentos(message.getIntentos() + 1);
        SendMessage request = new SendMessage(message.getChatId(), message.getContent()).parseMode(com.pengrad.telegrambot.model.request.ParseMode.HTML);
        bot.execute(request);
        message.setEstadoEnvio(MessageStatus.ENVIADO);
        message.setFechaEnvio(LocalDateTime.now());
        logger.info("Successfully sent Telegram message to chatId: {}", message.getChatId());
        messageRepository.save(message);
    }

    @Recover
    public void recover(Exception e, Message message) {
        logger.error("Failed to send Telegram message to chatId: {} after multiple retries. Reason: {}", message.getChatId(), e.getMessage());
        message.setEstadoEnvio(MessageStatus.FALLIDO);
        messageRepository.save(message);
    }
}
