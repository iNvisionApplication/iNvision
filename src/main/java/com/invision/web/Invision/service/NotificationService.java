package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.dto.SystemNotificationDTO;
import com.invision.web.Invision.enums.NotificationReason;
import com.invision.web.Invision.event.LoanRequestEvent;
import com.invision.web.Invision.mapper.NotificationMapper;
import com.invision.web.Invision.model.SystemNotification;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.SystemNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SystemNotificationRepository systemNotificationRepository;
    private final JavaMailSender mailSender;
    private final com.invision.web.Invision.repository.UserRepository userRepository;
    private final NotificationMapper notificationMapper;

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

    public List<SystemNotificationDTO> getSystemNotifications() {
        User user = getAuthenticatedUser();


        if (user == null) {
            return Collections.emptyList();
        }

        return systemNotificationRepository.findByUserIdAndIsRead(user.getUserId(), false)
                .stream()
                .map(notificationMapper::systemNotificationToSystemNotificationDTO)
                .toList();
    }

    public void markAsRead(Long notificationId) throws Exception {
        SystemNotification notification = systemNotificationRepository.findById(notificationId).orElseThrow(() -> new Exception("No such notification"));
        notification.setRead(true);
        systemNotificationRepository.save(notification);
    }

    //send notifications after successful
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLoanRequest(LoanRequestEvent event){
        String message = "A loan for "+event.getAssetTitle()+"was requested by "+ event.getRequesterEmail() ;

        sendSystemNotification(event.getRequesterId(),
                NotificationReason.LOAN_REQUEST, message);

        if (event.getManagerOneEmail() != null) {
            sendEmailNotification(event.getManagerOneEmail(),
                    NotificationReason.LOAN_REQUEST, message);
        }
        if (event.getManagerTwoEmail() != null) {
            sendEmailNotification(event.getManagerTwoEmail(),
                    NotificationReason.LOAN_REQUEST, message);
        }

    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }

        // Return null if it's a String ("anonymousUser")
        return null;
    }
}