package com.institucion.ticketero.module_workday.api;

import com.institucion.ticketero.module_workday.application.WorkdayService;
import com.institucion.ticketero.module_workday.domain.Workday;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/workdays")
public class WorkdayController {

    private final WorkdayService workdayService;

    public WorkdayController(WorkdayService workdayService) {
        this.workdayService = workdayService;
    }

    @PostMapping("/open")
    public ResponseEntity<WorkdayResponse> openWorkday() {
        Workday newWorkday = workdayService.openNewWorkday();
        return new ResponseEntity<>(new WorkdayResponse(newWorkday.getId(), newWorkday.getStartTime(), newWorkday.getEndTime(), newWorkday.getStatus()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<WorkdayResponse> closeWorkday(@PathVariable UUID id) {
        Workday closedWorkday = workdayService.closeWorkday(id);
        return ResponseEntity.ok(new WorkdayResponse(closedWorkday.getId(), closedWorkday.getStartTime(), closedWorkday.getEndTime(), closedWorkday.getStatus()));
    }

    @GetMapping("/current")
    public ResponseEntity<WorkdayResponse> getCurrentActiveWorkday() {
        Workday currentWorkday = workdayService.getCurrentActiveWorkday();
        return ResponseEntity.ok(new WorkdayResponse(currentWorkday.getId(), currentWorkday.getStartTime(), currentWorkday.getEndTime(), currentWorkday.getStatus()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkdayResponse> getWorkdayById(@PathVariable UUID id) {
        Workday workday = workdayService.getWorkdayById(id);
        return ResponseEntity.ok(new WorkdayResponse(workday.getId(), workday.getStartTime(), workday.getEndTime(), workday.getStatus()));
    }
}