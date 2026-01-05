package com.fd.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fd.payment.dto.PaymentRequest;
import com.fd.payment.dto.PaymentResponse;
import com.fd.payment.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public PaymentResponse createPayment(
            @RequestBody PaymentRequest request) {

        return paymentService.createPayment(request);
    }

    @GetMapping("/order/{orderId}")
    public PaymentResponse getPaymentByOrder(@PathVariable Long orderId) {

        return paymentService.getPaymentByOrder(orderId);
    }
}
