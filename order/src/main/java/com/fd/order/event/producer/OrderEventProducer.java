package com.fd.order.event.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.fd.events.OrderCancelledEvent;
import com.fd.events.OrderEvent;
import com.fd.events.PaymentMethod;
import com.fd.order.entity.Order;

@Component
public class OrderEventProducer {
	private static final Logger log =LoggerFactory.getLogger(OrderEventProducer.class);
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishOrderCreated(Order order, PaymentMethod paymentMethod) {
        OrderEvent event = new OrderEvent(
                order.getId(),
                order.getTotalAmount(),
                paymentMethod
        );
        log.info("Publishing ORDER_CREATED event for orderId={}, paymentMethod={}",order.getId(), paymentMethod);
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            kafkaTemplate.send(MessageBuilder.withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, "order-events")
                    .setHeader("X-Trace-Id", traceId)
                    .build());
            return;
        }
        kafkaTemplate.send("order-events", event);
    }
	public void publishOrderCancelled(Long orderId) {
		
	    log.info("Publishing ORDER_CANCELLED event for orderId={}", orderId);
	    OrderCancelledEvent event = new OrderCancelledEvent(orderId);
        String traceId = MDC.get("traceId");
        if (traceId != null && !traceId.isBlank()) {
            kafkaTemplate.send(MessageBuilder.withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, "order-events")
                    .setHeader("X-Trace-Id", traceId)
                    .build());
            return;
        }
	    kafkaTemplate.send("order-events", event);
	}

}
