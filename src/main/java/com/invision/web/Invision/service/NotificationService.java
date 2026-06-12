package com.invision.web.Invision.service;

import com.invision.web.Invision.enums.NotificationReason;
import com.invision.web.Invision.model.SystemNotification;
import com.invision.web.Invision.repository.SystemNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SystemNotificationRepository systemNotificationRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    public void sendSystemNotification(Long userId, NotificationReason reason, String message) {
        SystemNotification notification = SystemNotification.builder()
                .userId(userId)
                .reason(reason)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        systemNotificationRepository.save(notification);
    }

    public void sendEmailNotification(String email, NotificationReason reason, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(email);
        mail.setSubject(reason.name());
        mail.setText(message);
        mailSender.send(mail);
    }


    public void sendAll(Long userId, String email, NotificationReason reason, String message) {
        sendSystemNotification(userId, reason, message);
        sendEmailNotification(email, reason, message);
    }
}