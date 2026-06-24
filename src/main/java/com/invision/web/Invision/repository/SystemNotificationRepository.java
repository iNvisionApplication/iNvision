package com.invision.web.Invision.repository;

import com.invision.web.Invision.model.SystemNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    List<SystemNotification> findByUserId(Long userId);
    List<SystemNotification> findByUserIdAndIsRead(Long userId, boolean isRead);
}