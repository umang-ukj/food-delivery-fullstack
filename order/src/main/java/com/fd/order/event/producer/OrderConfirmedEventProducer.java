package com.fd.order.event.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
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
    	String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            kafkaTemplate.send(MessageBuilder.withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, "order-confirmed-events")
                    .setHeader("X-Trace-Id", traceId)
                    .build());
        } else {
            kafkaTemplate.send("order-confirmed-events",event);
        }
        log.info("Publishing ORDER_CONFIRMED event for orderId={}", event.getOrderId());

    }
}

