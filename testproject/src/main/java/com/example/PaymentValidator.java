package com.example;

import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {

    public void validate(String request) {
        if (request == null || request.isEmpty()) {
            throw new IllegalArgumentException("Request cannot be empty");
        }
        validateFormat(request);
        validateAmount(request);
    }

    private void validateFormat(String request) {
        // Format validation logic
    }

    private void validateAmount(String request) {
        // Amount validation logic
    }
}
