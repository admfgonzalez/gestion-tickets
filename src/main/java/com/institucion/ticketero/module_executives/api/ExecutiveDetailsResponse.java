package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_queues.domain.AttentionType;

import java.util.Set;

public record ExecutiveDetailsResponse(
    Long id,
    String fullName,
    String module,
    ExecutiveStatus status,
    Set<AttentionType> supportedAttentionTypes
) {}
