package com.fd.payment.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fd.events.OrderEvent;
import com.fd.payment.service.PaymentService;

//listen to orders
@Component
public class OrderEventConsumer {

    private final PaymentService paymentService;

    public OrderEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-events")
    public void handleOrderEvent(OrderEvent event) {
        if ("CREATED".equals(event.getStatus())) {
            //log.info("Received ORDER_CREATED event for orderId={}", event.getOrderId());
            // process payment
        	paymentService.processPayment(event);
        }
    }

}

