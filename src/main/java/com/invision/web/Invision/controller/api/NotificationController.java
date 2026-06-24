package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.SystemNotificationDTO;
import com.invision.web.Invision.model.SystemNotification;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<SystemNotificationDTO>> getSystemNotifications(){
        return ResponseEntity.ok(notificationService.getSystemNotifications());
    }

    @PatchMapping("/read")
    public void markAsRead(Long notificationId) throws Exception {
        notificationService.markAsRead(notificationId);
    }
}
