package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_queues.domain.AttentionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateExecutiveRequest(
    @NotBlank String fullName,
    @NotBlank String module,
    @NotNull @Size(min = 1) Set<AttentionType> supportedAttentionTypes
) {}
