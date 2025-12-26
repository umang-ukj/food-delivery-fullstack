package com.fd.payment.service;

import org.springframework.stereotype.Service;
import com.fd.payment.model.OrderEvent;
import com.fd.payment.producer.PaymentEventProducer;

@Service
//payment processing core logic
public class PaymentService {

    private final PaymentEventProducer producer;

    public PaymentService(PaymentEventProducer producer) {
        this.producer = producer;
    }

    public void processPayment(OrderEvent event) {

        boolean paymentSuccess = simulatePayment(event.getAmount());

        if (paymentSuccess) {
            producer.sendPaymentResult(event.getOrderId(), "SUCCESS");
        } else {
            producer.sendPaymentResult(event.getOrderId(), "FAILED");
        }
    }

    private boolean simulatePayment(Double amount) {
        return amount < 1000; // simple rule for demo
    }
}
