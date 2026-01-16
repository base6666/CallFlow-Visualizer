package com.example;

import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    public void save(Payment payment) {
        // Simulate DB save
        payment.setId("PAY-" + System.currentTimeMillis());
        System.out.println("Saved: " + payment.getId());
    }

    public Payment findById(String id) {
        // Simulate DB query
        Payment payment = new Payment();
        payment.setId(id);
        payment.setAmount(100);
        return payment;
    }
}
