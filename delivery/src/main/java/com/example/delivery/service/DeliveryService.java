package com.example.delivery.service;

import org.springframework.stereotype.Service;

import com.example.delivery.producer.DeliveryEventProducer;

@Service
public class DeliveryService {

    private final DeliveryEventProducer producer;

    public DeliveryService(DeliveryEventProducer producer) {
        this.producer = producer;
    }

    public void assignDelivery(Long orderId) {

        // Simulate agent assignment
        producer.sendDeliveryUpdate(orderId, "OUT_FOR_DELIVERY");

        // Simulate delivery completion
        producer.sendDeliveryUpdate(orderId, "DELIVERED");
    }
}
