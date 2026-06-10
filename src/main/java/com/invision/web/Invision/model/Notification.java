package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.NotificationReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


@Getter
public sealed class Notification permits SystemNotification, EmailNotification {

    private final Long userId;
    private final NotificationReason reason;
    private final String message;
    private final LocalDateTime createdAt;

    public Notification(Long userId, NotificationReason reason, String message) {
        this.userId = userId;
        this.reason = reason;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
