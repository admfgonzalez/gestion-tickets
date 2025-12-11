package com.institucion.ticketero.module_workday.application;

import com.institucion.ticketero.common.exceptions.ResourceNotFoundException;
import com.institucion.ticketero.module_workday.domain.Workday;
import com.institucion.ticketero.module_workday.domain.WorkdayStatus;
import com.institucion.ticketero.module_workday.infrastructure.WorkdayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class WorkdayService {

    private final WorkdayRepository workdayRepository;

    public WorkdayService(WorkdayRepository workdayRepository) {
        this.workdayRepository = workdayRepository;
    }

    @Transactional
    public Workday openNewWorkday() {
        Optional<Workday> activeWorkday = workdayRepository.findByStatus(WorkdayStatus.OPEN);
        activeWorkday.ifPresent(workday -> {
            workday.close();
            workdayRepository.save(workday);
        }); // Close and save any previously active workday

        Workday newWorkday = new Workday(LocalDateTime.now(), null, WorkdayStatus.OPEN);
        return workdayRepository.save(newWorkday);
    }

    @Transactional
    public Workday closeWorkday(Long id) {
        Workday workday = workdayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workday not found with id: " + id));
        workday.close();
        return workdayRepository.save(workday);
    }

    @Transactional(readOnly = true)
    public Workday getCurrentActiveWorkday() {
        return workdayRepository.findByStatus(WorkdayStatus.OPEN)
                .orElseGet(() -> {
                    // If no active workday is found, open a new one
                    return openNewWorkday();
                });
    }

    @Transactional(readOnly = true)
    public Workday getWorkdayById(Long id) {
        return workdayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workday not found with id: " + id));
    }
}