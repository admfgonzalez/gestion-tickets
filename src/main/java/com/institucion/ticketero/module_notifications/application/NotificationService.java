package com.institucion.ticketero.module_notifications.application;

import com.institucion.ticketero.module_tickets.domain.Ticket;

/**
 * Q-Insight: Application Service Interface for Notifications.
 * This interface defines the contract for sending notifications to customers, abstracting the specific delivery mechanism (e.g., Telegram).
 * It supports the key notification events required by the business logic (RF-002).
 */
public interface NotificationService {

    /**
     * Q-Insight: Sends a confirmation message when a ticket is created.
     * @param ticket The newly created ticket.
     * @param positionInQueue The ticket's initial position in the queue.
     * @param estimatedWaitTimeMinutes The initial estimated wait time.
     */
    void sendTicketConfirmation(Ticket ticket, int positionInQueue, long estimatedWaitTimeMinutes);

    /**
     * Q-Insight: Sends a pre-arrival alert when the customer is nearing the front of the queue.
     * @param ticket The ticket that is a few places away from being called.
     */
    void sendPreArrivalAlert(Ticket ticket);

    /**
     * Q-Insight: Sends an alert when the ticket is called to be served.
     * @param ticket The ticket that has just been assigned to an executive.
     */
    void sendTurnActiveAlert(Ticket ticket);
}
