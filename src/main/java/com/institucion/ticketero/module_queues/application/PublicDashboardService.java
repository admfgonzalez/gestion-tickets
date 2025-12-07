package com.institucion.ticketero.module_queues.application;

import com.institucion.ticketero.module_queues.api.NowServingTicket;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for the public-facing dashboard.
 */
@Service
public class PublicDashboardService {

    private final TicketRepository ticketRepository;

    public PublicDashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Gets the data for the simple "Now Serving" dashboard.
     * @return A list of tickets currently being attended.
     */
    public List<NowServingTicket> getNowServing() {
        return ticketRepository
                .findAllByStatusOrderByAttendedAtDesc(TicketStatus.ATTENDING)
                .stream()
                .map(ticket -> new NowServingTicket(
                        ticket.getTicketNumber(),
                        ticket.getExecutive().getModule()
                ))
                .collect(Collectors.toList());
    }
}
