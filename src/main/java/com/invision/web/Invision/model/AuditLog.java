package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.ActionLog;
import com.invision.web.Invision.enums.EntityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Table(name="audit_log")
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(name="user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private EntityType entityType; //asset or loan


    @NotNull
    private Long entityId;

    @Enumerated(EnumType.STRING)
    private ActionLog action;

    @NotNull
    private LocalDateTime timeStamp;

    private String oldValue;

    private String newValue;
}
