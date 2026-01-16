package com.example;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public String processPayment(@RequestBody String request) {
        return paymentService.process(request);
    }

    @GetMapping("/{id}")
    public String getPayment(@PathVariable String id) {
        return paymentService.findById(id);
    }
}
