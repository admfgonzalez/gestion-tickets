package com.institucion.ticketero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Q-Insight: Main Application Class.
 * This is the entry point for the Spring Boot application.
 * - @SpringBootApplication: Enables auto-configuration, component scanning, and property support.
 * - @EnableScheduling: Activates Spring's scheduled task execution capabilities, used for background jobs like queue monitoring.
 * - @EnableAsync: Enables Spring's asynchronous method execution capability, used for non-blocking tasks like sending notifications.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TicketeroApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketeroApplication.class, args);
    }

}
