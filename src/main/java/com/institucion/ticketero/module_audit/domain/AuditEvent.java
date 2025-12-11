package com.institucion.ticketero.module_audit.domain;

public enum AuditEvent {
    TICKET_CREADO,
    TICKET_ASIGNADO,
    TICKET_COMPLETADO,
    TICKET_CANCELADO,
    MENSAJE_ENVIADO,
    MENSAJE_FALLIDO,
    ADVISOR_STATUS_CHANGED,
    POSITION_RECALCULATED
}
