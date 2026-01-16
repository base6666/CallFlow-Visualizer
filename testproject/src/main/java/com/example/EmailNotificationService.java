package com.example;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
        logNotification(message);
    }

    private void logNotification(String message) {
        System.out.println("Logged: " + message);
    }
}
