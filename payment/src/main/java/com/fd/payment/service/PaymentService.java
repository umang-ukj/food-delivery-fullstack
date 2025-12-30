package com.fd.payment.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fd.payment.model.OrderEvent;
import com.fd.payment.producer.PaymentEventProducer;

@Service
//payment processing core logic
public class PaymentService {
	private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    
    private final PaymentEventProducer producer;

    public PaymentService(PaymentEventProducer producer) {
        this.producer = producer;
    }

 

    public void processPayment(OrderEvent event) {

        log.info("Received ORDER_CREATED event for orderId={}", event.getOrderId());

        boolean paymentSuccess = simulatePayment(event.getAmount());

        if (paymentSuccess) {
            log.info("Payment SUCCESS for orderId={}", event.getOrderId());
            producer.sendPaymentResult(event.getOrderId(), "SUCCESS");
        } else {
            log.warn("Payment FAILED for orderId={}", event.getOrderId());
            producer.sendPaymentResult(event.getOrderId(), "FAILED");
        }
    }
    private boolean simulatePayment(Double amount) {
        return amount < 1000; // simple rule for demo
    }
}
