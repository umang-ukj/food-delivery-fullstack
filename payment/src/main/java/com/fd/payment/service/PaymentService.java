package com.fd.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fd.events.OrderEvent;
import com.fd.payment.entity.Payment;
import com.fd.payment.producer.PaymentEventProducer;
import com.fd.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentEventProducer producer;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentEventProducer producer,
                          PaymentRepository paymentRepository) {
        this.producer = producer;
        this.paymentRepository = paymentRepository;
    }

    public void processPayment(OrderEvent event) {

        log.info("Received ORDER_CREATED event for orderId={}", event.getOrderId());

        boolean paymentSuccess = simulatePayment(event.getAmount());
        String status = paymentSuccess ? "PAID" : "FAILED";

        
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setAmount(event.getAmount());
        payment.setStatus(status);

        paymentRepository.save(payment);

        log.info("Payment saved in DB for orderId={} with status={}",
                 event.getOrderId(), status);

        // ✅ SEND EVENT
        producer.sendPaymentResult(event.getOrderId(), status);
        log.info("Payment event sent for orderId={}", event.getOrderId());
    }

    private boolean simulatePayment(Double amount) {
        return amount < 1000;
    }
}
