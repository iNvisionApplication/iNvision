package com.invision.web.Invision.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long logId;

    @ManyToOne
    private long userId;

    private Entity entityType; //asset or loan

    @ManyToOne
    private long entityId;

    private Action action;

    private LocalDateTime timeStamp;

    private String oldValue;

    private String newValue;
}
