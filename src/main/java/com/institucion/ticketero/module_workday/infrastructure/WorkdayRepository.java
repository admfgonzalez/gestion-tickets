package com.institucion.ticketero.module_workday.infrastructure;

import com.institucion.ticketero.module_workday.domain.Workday;

import com.institucion.ticketero.module_workday.domain.WorkdayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorkdayRepository extends JpaRepository<Workday, Long> {
    Optional<Workday> findByStatus(WorkdayStatus status);
}