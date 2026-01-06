package com.fd.order.event.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fd.events.DeliveryEvent;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.repository.OrderRepository;

@Component
public class DeliveryEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(DeliveryEventConsumer.class);

    private final OrderRepository repository;

    public DeliveryEventConsumer(OrderRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
    	    topics = "delivery-events",
    	    containerFactory = "deliveryKafkaListenerContainerFactory"
    	)
    	public void handleDeliveryEvent(DeliveryEvent event) {

    	    log.info(
    	        "Received DELIVERY_{} event for orderId={}",
    	        event.getStatus(),
    	        event.getOrderId()
    	    );

    	    Order order = repository.findById(event.getOrderId())
    	            .orElseThrow(() ->
    	                    new RuntimeException("Order not found: " + event.getOrderId())
    	            );

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
    	            order.setStatus(OrderStatus.DELIVERED);
    	            log.info("Order {} marked as DELIVERED", order.getId());
    	        }
    	    }

    	    repository.save(order);
    	}

}

