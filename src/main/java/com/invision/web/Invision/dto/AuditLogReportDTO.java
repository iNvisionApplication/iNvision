package com.invision.web.Invision.dto;

import java.time.LocalDateTime;

public record AuditLogReportDTO(
        Long logId,
        String operatorEmail,
        String entityType,
        Long entityId,
        String action,
        String details,
        LocalDateTime timestamp
) {}