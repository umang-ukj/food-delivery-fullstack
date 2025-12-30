package com.fd.order.event.consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.entity.PaymentEvent;
import com.fd.order.repository.OrderRepository;

@Component
public class PaymentEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderRepository repository;

    public PaymentEventConsumer(OrderRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "payment-events")
    public void handlePaymentEvent(PaymentEvent event) {

        log.info("Received PAYMENT_{} event for orderId={}",
                event.getStatus(), event.getOrderId());

        Order order = repository.findById(event.getOrderId())
                .orElseThrow();

        if ("SUCCESS".equals(event.getStatus())) {
            order.setStatus(OrderStatus.PAID);
            log.info("Order {} marked as PAID", order.getId());
        } else {
            order.setStatus(OrderStatus.FAILED);
            log.warn("Order {} marked as FAILED", order.getId());
        }

        repository.save(order);
    }
}
