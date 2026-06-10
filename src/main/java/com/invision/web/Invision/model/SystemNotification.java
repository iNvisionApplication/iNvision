package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.NotificationReason;

import java.time.LocalDateTime;

public final class SystemNotification extends Notification {
    public SystemNotification(Long userId, NotificationReason reason, String message, LocalDateTime createdAt) {
        super(userId, reason, message, createdAt);
    }
}
