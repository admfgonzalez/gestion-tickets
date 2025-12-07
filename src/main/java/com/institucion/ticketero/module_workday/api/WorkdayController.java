package com.institucion.ticketero.module_workday.api;

import com.institucion.ticketero.module_workday.application.WorkdayService;
import com.institucion.ticketero.module_workday.domain.Workday;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workday")
public class WorkdayController {

    private final WorkdayService workdayService;

    public WorkdayController(WorkdayService workdayService) {
        this.workdayService = workdayService;
    }

    @PostMapping("/start")
    public ResponseEntity<Workday> startNewDay() {
        try {
            return ResponseEntity.ok(workdayService.startNewDay());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build(); // 409 Conflict
        }
    }

    @PostMapping("/end")
    public ResponseEntity<Workday> endCurrentDay() {
        try {
            return ResponseEntity.ok(workdayService.endCurrentDay());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).build(); // 409 Conflict
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Workday> getActiveWorkday() {
        return workdayService.getActiveWorkday()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<Workday>> getWorkdayHistory() {
        return ResponseEntity.ok(workdayService.getWorkdayHistory());
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> getWorkdayReport(@PathVariable UUID id) {
        try {
            byte[] pdfContents = workdayService.generateWorkdayReport(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "reporte-jornada-" + id + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdfContents);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
