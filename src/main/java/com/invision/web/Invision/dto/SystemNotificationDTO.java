package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.NotificationReason;

public record SystemNotificationDTO(Long notificationId, NotificationReason reason,String message) {
}
