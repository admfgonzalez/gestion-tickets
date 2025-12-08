package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_queues.domain.AttentionType;

import java.util.Set;
import java.util.UUID;

public record ExecutiveDetailsResponse(
    UUID id,
    String fullName,
    String module,
    ExecutiveStatus status,
    Set<AttentionType> supportedAttentionTypes
) {}
