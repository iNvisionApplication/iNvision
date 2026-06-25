package com.invision.web.Invision.mapper;

import com.invision.web.Invision.dto.SystemNotificationDTO;
import com.invision.web.Invision.model.SystemNotification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public SystemNotificationDTO systemNotificationToSystemNotificationDTO(SystemNotification notification){
        if (notification == null) {
            return null;
        }
        return new SystemNotificationDTO(
               notification.getId(), notification.getReason(),notification.getMessage()
        );
    }
}
