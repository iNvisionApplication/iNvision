package com.invision.web.Invision.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Table(name="audit_log")
@Entity
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

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
