package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_executives.application.ExecutiveService;
import com.institucion.ticketero.module_tickets.application.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/executives")
public class ExecutiveController {

    private final TicketService ticketService;
    private final ExecutiveService executiveService;

    public ExecutiveController(TicketService ticketService, ExecutiveService executiveService) {
        this.ticketService = ticketService;
        this.executiveService = executiveService;
    }

    @PostMapping("/{id}/end-ticket")
    public ResponseEntity<Void> endCurrentTicket(@PathVariable UUID id) {
        ticketService.closeCurrentTicketForExecutive(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/attention-types")
    public ResponseEntity<Void> updateAttentionTypes(@PathVariable UUID id, @RequestBody UpdateAttentionTypesRequest request) {
        executiveService.updateSupportedAttentionTypes(id, request.attentionTypes());
        return ResponseEntity.ok().build();
    }
}
