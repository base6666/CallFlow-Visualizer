package com.example;

/**
 * Interface for notification services.
 * Multiple implementations: Email, SMS, Push
 */
public interface NotificationService {
    void send(String message);
}
