package com.institucion.ticketero.module_notifications.api;

import com.institucion.ticketero.module_notifications.application.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/telegram-bot-username")
    public ResponseEntity<String> getTelegramBotUsername() {
        return ResponseEntity.ok(notificationService.getBotUsername());
    }
}
