package com.institucion.ticketero.module_workday.api;

import com.institucion.ticketero.module_workday.domain.WorkdayStatus;

import java.time.LocalDateTime;

public record WorkdayResponse(
        Long id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        WorkdayStatus status
) {}
