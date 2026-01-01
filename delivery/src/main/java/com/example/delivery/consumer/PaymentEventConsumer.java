package com.example.delivery.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fd.events.DeliveryEvent;
import com.fd.events.PaymentEvent;


@Component
public class PaymentEventConsumer {
	private static final Logger log =
		    LoggerFactory.getLogger(PaymentEventConsumer.class);

	private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

	public PaymentEventConsumer(KafkaTemplate<String, DeliveryEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@KafkaListener(topics = "payment-events")
	public void consume(PaymentEvent event) {

	    log.info("Received PAYMENT_{} for orderId={}",event.getStatus(), event.getOrderId());

	    if ("PAID".equals(event.getStatus())) {
	        kafkaTemplate.send(
	            "delivery-events",
	            new DeliveryEvent(event.getOrderId(), "OUT_FOR_DELIVERY")
	        );

	        kafkaTemplate.send(
	            "delivery-events",
	            new DeliveryEvent(event.getOrderId(), "DELIVERED")
	        );
	    }
	}

}

