package com.institucion.ticketero.module_audit.application;

import com.institucion.ticketero.module_audit.domain.AuditEvent;
import com.institucion.ticketero.module_audit.domain.AuditLog;
import com.institucion.ticketero.module_audit.infrastructure.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void recordEvent(AuditEvent eventType, String actor, String entityType, Long entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setEventType(eventType);
        auditLog.setActor(actor);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }
}
