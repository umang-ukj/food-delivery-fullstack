package com.fd.order.event.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.events.OrderEvent;
import com.fd.order.entity.Order;

@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}

	public void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus().name()
        );
        kafkaTemplate.send("order-events", event);
    }
}
