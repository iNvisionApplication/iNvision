package com.invision.web.Invision.model;

import com.invision.web.Invision.enums.NotificationReason;

import java.time.LocalDateTime;

public final class EmailNotification extends Notification {

    private final String recipientName;
    private boolean isSent;

    public EmailNotification(Long userId, NotificationReason reason, String message, LocalDateTime createdAt, String recipientEmail) {
        super(userId, reason, message);
        this.recipientName = recipientEmail;
        isSent = false;
    }

    public boolean isSent(){return isSent;}
    public void markAsSent(){isSent=true;}
    public String getGetRecipientName(){return recipientName;}


}
