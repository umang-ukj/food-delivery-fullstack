package com.fd.order.event.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.order.dto.OrderConfirmedEvent;

@Component
public class OrderConfirmedEventProducer {

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public OrderConfirmedEventProducer(
            KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(Long orderId) {
        kafkaTemplate.send(
            "order-confirmed-events",
            new OrderConfirmedEvent(orderId)
        );
    }
}

