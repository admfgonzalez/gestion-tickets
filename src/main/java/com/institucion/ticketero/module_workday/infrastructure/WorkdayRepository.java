package com.institucion.ticketero.module_workday.infrastructure;

import com.institucion.ticketero.module_workday.domain.Workday;
import com.institucion.ticketero.module_workday.domain.WorkdayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface WorkdayRepository extends JpaRepository<Workday, UUID> {
    Optional<Workday> findFirstByStatusOrderByStartTimeDesc(WorkdayStatus status);
    List<Workday> findTop10ByOrderByStartTimeDesc();
}
