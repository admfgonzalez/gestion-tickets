package com.institucion.ticketero.module_workday.application;

import com.institucion.ticketero.common.exceptions.ResourceNotFoundException;
import com.institucion.ticketero.module_reports.application.PdfReportService;
import com.institucion.ticketero.module_tickets.application.TicketService;
import com.institucion.ticketero.module_tickets.domain.Ticket;
import com.institucion.ticketero.module_tickets.infrastructure.TicketRepository;
import com.institucion.ticketero.module_workday.domain.Workday;
import com.institucion.ticketero.module_workday.domain.WorkdayStatus;
import com.institucion.ticketero.module_workday.infrastructure.WorkdayRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkdayService {

    private final WorkdayRepository workdayRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final PdfReportService pdfReportService;

    public WorkdayService(WorkdayRepository workdayRepository, TicketRepository ticketRepository, TicketService ticketService, PdfReportService pdfReportService) {
        this.workdayRepository = workdayRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.pdfReportService = pdfReportService;
    }

    public Optional<Workday> getActiveWorkday() {
        return workdayRepository.findFirstByStatusOrderByStartTimeDesc(WorkdayStatus.ACTIVE);
    }

    public List<Workday> getWorkdayHistory() {
        return workdayRepository.findTop10ByOrderByStartTimeDesc();
    }

    @Transactional
    public Workday startNewDay() {
        Optional<Workday> activeWorkday = getActiveWorkday();
        if (activeWorkday.isPresent()) {
            throw new IllegalStateException("A workday is already active.");
        }

        // Reset ticket counter
        ticketService.resetTicketCounter();

        Workday newWorkday = new Workday();
        newWorkday.setStartTime(LocalDateTime.now());
        newWorkday.setStatus(WorkdayStatus.ACTIVE);
        return workdayRepository.save(newWorkday);
    }

    @Transactional
    public Workday endCurrentDay() {
        Workday activeWorkday = getActiveWorkday()
                .orElseThrow(() -> new IllegalStateException("No active workday to end."));
        
        activeWorkday.setEndTime(LocalDateTime.now());
        activeWorkday.setStatus(WorkdayStatus.ENDED);
        return workdayRepository.save(activeWorkday);
    }

    public byte[] generateWorkdayReport(UUID workdayId) throws IOException {
        Workday workday = workdayRepository.findById(workdayId)
                .orElseThrow(() -> new ResourceNotFoundException("Workday not found with ID: " + workdayId));

        LocalDateTime endTime = workday.getEndTime() != null ? workday.getEndTime() : LocalDateTime.now();
        List<Ticket> tickets = ticketRepository.findAllByCreatedAtBetweenOrderByCreatedAtAsc(workday.getStartTime(), endTime);

        return pdfReportService.generateWorkdayReport(workday, tickets);
    }
}
