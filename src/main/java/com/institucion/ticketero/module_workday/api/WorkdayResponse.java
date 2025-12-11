package com.institucion.ticketero.module_workday.api;

import com.institucion.ticketero.module_workday.domain.WorkdayStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WorkdayResponse(
        UUID id,
        LocalDateTime startTime,
        LocalDateTime endTime,
        WorkdayStatus status
) {}
