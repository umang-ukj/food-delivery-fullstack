package com.example.delivery.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.delivery.producer.DeliveryEventProducer;
import com.example.delivery.service.DeliveryService;
import com.fd.events.OrderConfirmedEvent;
import com.fd.events.PaymentEvent;
import com.fd.events.PaymentStatus;


@Component
public class OrderConfirmedEventConsumer {
	private static final Logger log =
		    LoggerFactory.getLogger(OrderConfirmedEventConsumer.class);
    private final DeliveryService service;
	private DeliveryEventProducer deliveryEventProducer;
	public OrderConfirmedEventConsumer(DeliveryEventProducer deliveryEventProducer, DeliveryService service) {
		this.deliveryEventProducer=deliveryEventProducer;
		this.service=service;
	}

	/*
	 * @KafkaListener(topics = "order-confirmed-events", containerFactory =
	 * "orderConfirmedKafkaListenerContainerFactory") public void
	 * handleOrderConfirmed(OrderConfirmedEvent event) {
	 * log.info("Received ORDER_CONFIRMED event for orderId={}",
	 * event.getOrderId());
	 * 
	 * 
	 * deliveryEventProducer.sendDeliveryUpdate( event.getOrderId(),
	 * DeliveryStatus.OUT_FOR_DELIVERY );
	 * 
	 * deliveryEventProducer.sendDeliveryUpdate( event.getOrderId(),
	 * DeliveryStatus.DELIVERED );
	 * 
	 * }
	 */
	@KafkaListener(topics = "order-confirmed-events",containerFactory ="orderConfirmedKafkaListenerContainerFactory")
			public void handlePaymentEvent(OrderConfirmedEvent event) {

			   

			    log.info("Payment confirmed. Starting delivery for orderId={}", event.getOrderId());

			    service.createDelivery(event.getOrderId());
			}

}

