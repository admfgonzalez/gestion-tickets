package com.institucion.ticketero.module_queues.api;

import com.institucion.ticketero.module_queues.application.QueueService;
import com.institucion.ticketero.module_workday.application.WorkdayService;
import com.institucion.ticketero.module_workday.domain.Workday;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/queues")
public class QueueController {

    private final QueueService queueService;
    private final WorkdayService workdayService;

    public QueueController(QueueService queueService, WorkdayService workdayService) {
        this.queueService = queueService;
        this.workdayService = workdayService;
    }

    @GetMapping
    public ResponseEntity<List<QueueStatusResponse>> getAllQueueStatus() {
        Optional<Workday> workday = Optional.ofNullable(workdayService.getCurrentActiveWorkday());
        List<QueueStatusResponse> response = queueService.getAllQueueStatus(workday.map(Workday::getStartTime));
        return ResponseEntity.ok(response);
    }
}
