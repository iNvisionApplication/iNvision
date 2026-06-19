package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.AuditLogReportDTO;
import com.invision.web.Invision.model.AuditLog;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.AuditLogRepository;
import com.invision.web.Invision.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AuditLogApiController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @GetMapping("/extract")
    public ResponseEntity<List<AuditLogReportDTO>> extractSystemTrail(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String action) {

        List<AuditLog> rawLogs = auditLogRepository.findAll();

        // ✅ FIXED: Removed 'th -> false' filter so lookups register correctly
        // ─── UPDATE THIS SPECIFIC PIECE INSIDE YOUR CONTROLLER ───
        Map<Long, String> userEmailMap = userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null)
                .collect(Collectors.toMap(
                        User::getUserId,
                        User::getEmail,
                        (existing, replacement) -> existing
                ));

        List<AuditLogReportDTO> extractedTrail = rawLogs.stream()
                .sorted((l1, l2) -> {
                    if (l1.getTimeStamp() == null && l2.getTimeStamp() == null) return 0;
                    if (l1.getTimeStamp() == null) return 1;
                    if (l2.getTimeStamp() == null) return -1;
                    return l2.getTimeStamp().compareTo(l1.getTimeStamp());
                })
                .filter(l -> entityType == null || entityType.trim().isEmpty() ||
                        (l.getEntityType() != null && l.getEntityType().name().equalsIgnoreCase(entityType.trim())))

                .filter(l -> action == null || action.trim().isEmpty() ||
                        (l.getAction() != null && l.getAction().name().equalsIgnoreCase(action.trim())))

                .map(l -> {
                    // ✅ FIXED: Added map verification step to resolve real user emails
                    String email = "SYSTEM / AUTOMATION";
                    if (l.getUserId() != null && userEmailMap.containsKey(l.getUserId())) {
                        email = userEmailMap.get(l.getUserId());
                    }

                    String compiledDetails = "Action performed on entity.";
                    if (l.getOldValue() != null || l.getNewValue() != null) {
                        compiledDetails = String.format("[Before]: %s | [After]: %s",
                                (l.getOldValue() != null ? l.getOldValue() : "N/A"),
                                (l.getNewValue() != null ? l.getNewValue() : "N/A"));
                    }

                    return new AuditLogReportDTO(
                            l.getLogId(),
                            email,
                            l.getEntityType() != null ? l.getEntityType().name() : "SYSTEM_EVENT",
                            l.getEntityId(),
                            l.getAction() != null ? l.getAction().name() : "UNKNOWN",
                            compiledDetails,
                            l.getTimeStamp()
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(extractedTrail);
    }
}