package com.klu.ecommerce_backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.klu.ecommerce_backend.model.BackgroundJob;

@Service
public class JobNotificationService {

    private final SimpMessagingTemplate messaging;

    public JobNotificationService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void publish(BackgroundJob job) {
        messaging.convertAndSend("/topic/jobs", job);
    }
}