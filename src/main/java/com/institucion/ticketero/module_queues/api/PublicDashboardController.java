package com.institucion.ticketero.module_queues.api;

import com.institucion.ticketero.module_queues.application.PublicDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API Controller for the public dashboard.
 */
@RestController
@RequestMapping("/api/public-dashboard")
public class PublicDashboardController {

    private final PublicDashboardService publicDashboardService;

    public PublicDashboardController(PublicDashboardService publicDashboardService) {
        this.publicDashboardService = publicDashboardService;
    }

    /**
     * Endpoint to get the "Now Serving" data.
     * @return A list of tickets currently being attended.
     */
    @GetMapping("/now-serving")
    public ResponseEntity<List<NowServingTicket>> getNowServing() {
        return ResponseEntity.ok(publicDashboardService.getNowServing());
    }
}
