package com.institucion.ticketero.module_workday.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record WorkdayOpenRequest(
        @NotNull
        LocalDateTime startTime
) {}
