package com.fd.order.event.consumer;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;
import com.fd.events.DeliveryEvent;
import com.fd.events.DeliveryStatus;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.repository.OrderRepository;
import com.fd.order.service.EmailService;

@Component
public class DeliveryEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final OrderRepository repository;
    private final EmailService emailService;
    public DeliveryEventConsumer(OrderRepository repository,EmailService emailService) {
        this.repository = repository;
        this.emailService=emailService;
    }

    @KafkaListener(topics = "delivery-events",containerFactory = "deliveryKafkaListenerContainerFactory")
    	public void handleDeliveryEvent(DeliveryEvent event, @Header(name = "X-Trace-Id", required = false) String traceId) {

    	if (traceId != null && !traceId.isBlank()) {
            MDC.put("traceId", traceId);
        }
        try {
        log.info(
	        "Received DELIVERY_{} event for orderId={}",
	        event.getStatus(),
	        event.getOrderId()
	    );

    	    Order order = repository.findById(event.getOrderId())
    	            .orElseThrow(() ->
    	                    new RuntimeException("Order not found: " + event.getOrderId())
    	            );
    	    if (order.getStatus() == OrderStatus.CANCELLED && event.getStatus() != DeliveryStatus.CANCELLED) {
                log.info("Skipping delivery update {} because order {} is already CANCELLED", event.getStatus(), order.getId());
                return;
            }

    	    switch (event.getStatus()) {

    	        case CREATED -> {
    	            order.setStatus(OrderStatus.CONFIRMED);
    	            log.info("Order {} marked as CONFIRMED", order.getId());
    	        }

    	        case PICKED_UP -> {
    	            order.setStatus(OrderStatus.PICKED_UP);
    	            log.info("Order {} marked as PICKED_UP", order.getId());
    	        }

    	        case OUT_FOR_DELIVERY -> {
    	            order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
    	            log.info("Order {} marked as OUT_FOR_DELIVERY", order.getId());
    	        }

    	        case DELIVERED -> {
    	            //order.setStatus(OrderStatus.DELIVERED);
    	            //log.info("Order {} marked as DELIVERED", order.getId());
    	        	boolean wasDelivered = order.getStatus() == OrderStatus.DELIVERED;
                    order.setStatus(OrderStatus.DELIVERED);
                    order.setDeliveredAt(LocalDateTime.now());
                    log.info("Order {} marked as DELIVERED", order.getId());

                    if (!wasDelivered && order.getUserEmail() != null && !order.getUserEmail().isBlank()) {
                        emailService.sendOrderDeliveredEmail(order.getUserEmail(), order);
                    }   
    	        }
    	        case CANCELLED -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    log.info("Order {} marked as CANCELLED", order.getId());
                }
    	    }

    	    repository.save(order);
    	}finally {
            MDC.remove("traceId");
        }

}}

