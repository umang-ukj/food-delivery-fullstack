package com.example.delivery.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.delivery.producer.DeliveryEventProducer;
import com.fd.events.DeliveryEvent;
import com.fd.events.OrderConfirmedEvent;
import com.fd.events.PaymentEvent;


@Component
public class OrderConfirmedEventConsumer {
	private static final Logger log =
		    LoggerFactory.getLogger(OrderConfirmedEventConsumer.class);

	private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;
	private DeliveryEventProducer deliveryEventProducer;
	public OrderConfirmedEventConsumer(KafkaTemplate<String, DeliveryEvent> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@KafkaListener(topics = "order-confirmed-events")
	public void handleOrderConfirmed(OrderConfirmedEvent event) {
		
		
		deliveryEventProducer.sendDeliveryUpdate(
	        event.getOrderId(),
	        "OUT_FOR_DELIVERY"
	    );

	    deliveryEventProducer.sendDeliveryUpdate(
	        event.getOrderId(),
	        "DELIVERED"
	    );
	
	}

}

