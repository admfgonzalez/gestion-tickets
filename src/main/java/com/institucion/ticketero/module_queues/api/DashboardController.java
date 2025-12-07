package com.institucion.ticketero.module_queues.api;

import com.institucion.ticketero.module_queues.application.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Q-Insight: API Controller for the Dashboard.
 * This controller provides the endpoint for the supervisor dashboard (RF-007).
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Q-Insight: Endpoint for fetching all dashboard metrics.
     * Handles GET requests and returns a composite DTO with a complete snapshot of the system's state.
     * @return A ResponseEntity containing the dashboard metrics.
     */
    @GetMapping
    public ResponseEntity<DashboardMetricsResponse> getDashboardMetrics() {
        DashboardMetricsResponse metrics = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }
}
