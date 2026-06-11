package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.NotificationReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailNotification {
    private final String recipientEmail;
    private final NotificationReason reason;
    private final String message;

}
