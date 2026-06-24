package com.invision.web.Invision.service;

import com.invision.web.Invision.enums.ActionLog;
import com.invision.web.Invision.enums.EntityType;
import com.invision.web.Invision.model.AuditLog;
import com.invision.web.Invision.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    // 📁 Instantiate the custom secure logback channel
    private static final Logger auditFileLogger = LoggerFactory.getLogger("com.invision.web.Invision.audit");
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(Long userId, EntityType entityType, Long entityId, ActionLog action, String oldValue, String newValue) {

        // ─── PIPE A: WRITE TO APPEND-ONLY TEXT FILE ───
        String logRow = String.format(
                "OPERATOR_ID=%s | MODULE=%s | RECORD_ID=%s | ACTION=%s | BEFORE={%s} | AFTER={%s}",
                userId != null ? userId : "SYSTEM",
                entityType != null ? entityType.name() : "UNKNOWN",
                entityId != null ? entityId : "N/A",
                action != null ? action.name() : "UNKNOWN",
                oldValue != null ? oldValue : "EMPTY",
                newValue != null ? newValue : "EMPTY"
        );
        auditFileLogger.info(logRow);

        // ─── PIPE B: WRITE TO REAL-TIME DATABASE ───
        try {
            AuditLog log = AuditLog.builder()
                    .userId(userId)
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .timeStamp(LocalDateTime.now())
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Fail-safe: Database issues will not derail your main runtime workflows
            auditFileLogger.error("CRITICAL: DB Audit log insert failed. File log preserved. Err: {}", e.getMessage());
        }
    }

    public void logCreate(Long userId, EntityType entityType, Long entityId, String details) {
        saveLog(userId, entityType, entityId, ActionLog.CREATE, null, details);
    }

    public void logUpdate(Long userId, EntityType entityType, Long entityId, String oldDetails, String newDetails) {
        saveLog(userId, entityType, entityId, ActionLog.UPDATE, oldDetails, newDetails);
    }

    public void logDelete(Long userId, EntityType entityType, Long entityId, String fallbackSnapshot) {
        saveLog(userId, entityType, entityId, ActionLog.DELETE, fallbackSnapshot, null);
    }

    public void logCheckOut(Long userId, Long loanId, String assetDetails) {
        saveLog(userId, EntityType.LOAN, loanId, ActionLog.CHECKOUT, "Status: AVAILABLE", "Status: LOANED | " + assetDetails);
    }

    public void logCheckIn(Long userId, Long loanId, String assetDetails) {
        saveLog(userId, EntityType.LOAN, loanId, ActionLog.CHECKIN, "Status: LOANED", "Status: AVAILABLE | " + assetDetails);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
}