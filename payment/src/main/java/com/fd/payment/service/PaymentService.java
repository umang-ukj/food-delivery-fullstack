package com.fd.payment.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void processPayment(OrderEvent event) {

        Long orderId = event.getOrderId();
        log.info("Received ORDER_CREATED event for orderId={}", orderId);

        
        Optional<Payment> existingPayment =
                paymentRepository.findByOrderId(orderId);

        if (existingPayment.isPresent()) {
            log.info("Payment already exists for orderId={}, skipping processing", orderId);
            return; //  prevents Kafka retry + DB conflict
        }

        boolean paymentSuccess = simulatePayment(event.getAmount());
        String status = paymentSuccess ? "PAID" : "FAILED";

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(event.getAmount());
        payment.setStatus(status);

        paymentRepository.save(payment);

        log.info("Payment saved in DB for orderId={} with status={}",
                 orderId, status);

        // sends event only on successful db commit
        producer.sendPaymentResult(orderId, status);
        log.info("Payment event sent for orderId={}", orderId);
    }

    private boolean simulatePayment(Double amount) {
        return amount < 1000;
    }
}
