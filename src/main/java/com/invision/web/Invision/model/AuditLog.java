package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.ActionLog;
import com.invision.web.Invision.enums.EntityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Table(name="auditLog")
@Entity
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    @JoinColumn(name="userId")
    private User user;

    @Column(name = "entityType")
    private EntityType entityType; //asset or loan

    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private ActionLog action;

    @Column(name = "oldValue")
    private String oldValue;

    @Column(name = "newValue")
    private String newValue;

    @Column(name = "entityId")
    private Long entityId;

    @Column(name = "timeStamp")
    private LocalDateTime timeStamp;
}
