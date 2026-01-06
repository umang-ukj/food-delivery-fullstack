package com.fd.order.event.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.events.OrderConfirmedEvent;


@Component
public class OrderConfirmedEventProducer {

	private static final Logger log =LoggerFactory.getLogger(OrderConfirmedEventProducer.class);

    private final KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate;

    public OrderConfirmedEventProducer(
            KafkaTemplate<String, OrderConfirmedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderConfirmedEvent event) {
        kafkaTemplate.send("order-confirmed-events",event);
        log.info("Publishing ORDER_CONFIRMED event for orderId={}", event.getOrderId());

    }
}

