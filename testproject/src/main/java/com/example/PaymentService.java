package com.example;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final NotificationService notificationService;
    private final PaymentValidator validator;

    public PaymentService(
            PaymentRepository repository,
            NotificationService notificationService,
            PaymentValidator validator) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.validator = validator;
    }

    @Transactional
    public String process(String request) {
        // Validate
        validator.validate(request);

        // Save
        Payment payment = new Payment();
        payment.setAmount(100);
        repository.save(payment);

        // Notify
        notificationService.send("Payment processed: " + payment.getId());

        return "OK";
    }

    public String findById(String id) {
        Payment payment = repository.findById(id);
        return payment != null ? payment.toString() : "Not found";
    }
}
