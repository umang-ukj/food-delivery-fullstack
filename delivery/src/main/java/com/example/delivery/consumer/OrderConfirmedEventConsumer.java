package com.example.delivery.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.delivery.producer.DeliveryEventProducer;
import com.example.delivery.service.DeliveryService;
import com.fd.events.OrderCancelledEvent;
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
	
	@KafkaListener(topics = "order-confirmed-events",containerFactory ="orderConfirmedKafkaListenerContainerFactory")
	public void handlePaymentEvent(OrderConfirmedEvent event,@Header(name = "X-Trace-Id", required = false) String traceId) {
		if (traceId != null && !traceId.isBlank()) {
			MDC.put("traceId", traceId);
		}
		try {

			log.info("Payment confirmed. Starting delivery for orderId={}", event.getOrderId());

			service.createDelivery(event.getOrderId());
		} finally {
			MDC.remove("traceId");
		}
	}
    
	@KafkaListener(topics = "order-events", containerFactory = "orderCancelledKafkaListenerContainerFactory")
    public void handleOrderCancelled(OrderCancelledEvent event, @Header(name = "X-Trace-Id", required = false) String traceId) {
		if (traceId != null && !traceId.isBlank()) {
            MDC.put("traceId", traceId);
        }
        try {
        log.info("Received ORDER_CANCELLED event for orderId={}", event.getOrderId());
        service.cancelDelivery(event.getOrderId());
        } finally {
            MDC.remove("traceId");
        }
    }
}

