package com.example.delivery.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.delivery.model.OrderEvent;
import com.example.delivery.service.DeliveryService;
//consumes data from order-events topic from kafka (async communication)
@Component
public class OrderEventConsumer {

    private final DeliveryService deliveryService;

    public OrderEventConsumer(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @KafkaListener(topics = "order-events")
    public void consume(OrderEvent event) {

        if ("PAID".equals(event.getStatus())) {
            deliveryService.assignDelivery(event.getOrderId());
        }
    }
}
