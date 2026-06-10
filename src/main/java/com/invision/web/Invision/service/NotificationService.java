package com.invision.web.Invision.service;

import com.invision.web.Invision.model.EmailNotification;
import com.invision.web.Invision.model.Notification;
import com.invision.web.Invision.model.SystemNotification;
import com.invision.web.Invision.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    public void sendNotification(Notification notification){
        if(notification instanceof EmailNotification){
            EmailNotification email = (EmailNotification) notification;
            sendEmailNotification(email);
        } else if(notification instanceof SystemNotification){
            SystemNotification sys = (SystemNotification) notification;
            saveSystemNotification(sys);
        }
    }

    public void saveSystemNotification(SystemNotification sys){
        repository.save(sys);
    }

    public void sendEmailNotification(EmailNotification email){

    }
}
