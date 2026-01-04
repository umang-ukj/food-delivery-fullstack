package com.example.delivery.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.delivery.model.DeliveryStatus;
import com.example.delivery.producer.DeliveryEventProducer;
import com.fd.events.DeliveryEvent;
import com.fd.events.OrderConfirmedEvent;
import com.fd.events.PaymentEvent;


@Component
public class OrderConfirmedEventConsumer {
	private static final Logger log =
		    LoggerFactory.getLogger(OrderConfirmedEventConsumer.class);

	private DeliveryEventProducer deliveryEventProducer;
	public OrderConfirmedEventConsumer(DeliveryEventProducer deliveryEventProducer) {
		this.deliveryEventProducer=deliveryEventProducer;
	}

	@KafkaListener(topics = "order-confirmed-events", containerFactory = "orderConfirmedKafkaListenerContainerFactory")
	public void handleOrderConfirmed(OrderConfirmedEvent event) {
		log.info("Received ORDER_CONFIRMED event for orderId={}", event.getOrderId());

		
		deliveryEventProducer.sendDeliveryUpdate(
	        event.getOrderId(),
	        DeliveryStatus.OUT_FOR_DELIVERY
	    );

	    deliveryEventProducer.sendDeliveryUpdate(
	        event.getOrderId(),
	        DeliveryStatus.DELIVERED
	    );
	
	}

}

