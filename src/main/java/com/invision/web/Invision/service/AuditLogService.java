package com.invision.web.Invision.service;

import com.invision.web.Invision.enums.ActionLog;
import com.invision.web.Invision.enums.EntityType;
import com.invision.web.Invision.model.AuditLog;
import com.invision.web.Invision.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Core internal method to handle the database write.
     * REQUIRES_NEW guarantees the log is written even if the calling method rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(Long userId, EntityType entityType, Long entityId, ActionLog action, String oldValue, String newValue) {
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
    }

    /**
     * Logs the initial creation of an asset or loan record.
     */
    public void logCreate(Long userId, EntityType entityType, Long entityId, String details) {
        saveLog(userId, entityType, entityId, ActionLog.CREATE, null, details);
    }

    /**
     * Logs updates made to an existing asset or loan.
     */
    public void logUpdate(Long userId, EntityType entityType, Long entityId, String oldDetails, String newDetails) {
        saveLog(userId, entityType, entityId, ActionLog.UPDATE, oldDetails, newDetails);
    }

    /**
     * Logs the soft or hard deletion of an asset.
     */
    public void logDelete(Long userId, EntityType entityType, Long entityId, String fallbackSnapshot) {
        saveLog(userId, entityType, entityId, ActionLog.DELETE, fallbackSnapshot, null);
    }

    /**
     * Specialized tracking for checking out an asset to a borrower.
     */
    public void logCheckOut(Long userId, Long loanId, String assetDetails) {
        saveLog(userId, EntityType.LOAN, loanId, ActionLog.CHECKOUT, "Status: AVAILABLE", "Status: LOANED | " + assetDetails);
    }

    /**
     * Specialized tracking for returning/checking in an asset.
     */
    public void logCheckIn(Long userId, Long loanId, String assetDetails) {
        saveLog(userId, EntityType.LOAN, loanId, ActionLog.CHECKIN, "Status: LOANED", "Status: AVAILABLE | " + assetDetails);
    }

    /**
     * Fetches the entire system audit trail for dashboard tracking.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * Filters logs targeting a specific object instance (e.g., all history for Asset #5).
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }
}
