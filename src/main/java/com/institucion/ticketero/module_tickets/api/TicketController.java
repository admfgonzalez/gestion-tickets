package com.institucion.ticketero.module_tickets.api;

import com.institucion.ticketero.module_tickets.application.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Q-Insight: API Controller for Tickets.
 * This controller exposes REST endpoints for all ticket-related operations.
 * It follows the principle of a thin controller, delegating all business logic to the TicketService.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Q-Insight: Endpoint for Creating a Ticket (RF-001).
     * Handles POST requests to create a new ticket.
     * The @Valid annotation triggers bean validation on the request body.
     *
     * @param request The request body, mapped to the CreateTicketRequest DTO.
     * @return A ResponseEntity containing the created ticket's info and an HTTP 201 status.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    /**
     * Q-Insight: Endpoint for Checking Ticket Status (RF-006).
     * Handles GET requests to retrieve the status of a specific ticket by its number.
     *
     * @param ticketNumber The ticket number from the URL path.
     * @return A ResponseEntity with the ticket's status details.
     */
    @GetMapping("/{ticketNumber}/status")
    public ResponseEntity<TicketStatusResponse> getTicketStatus(@PathVariable String ticketNumber) {
        TicketStatusResponse response = ticketService.getTicketStatus(ticketNumber);
        return ResponseEntity.ok(response);
    }
}
