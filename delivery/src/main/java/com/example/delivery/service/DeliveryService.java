package com.example.delivery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.delivery.producer.DeliveryEventProducer;

@Service
public class DeliveryService {

    private static final Logger log =
            LoggerFactory.getLogger(DeliveryService.class);

    private final DeliveryEventProducer producer;

    public DeliveryService(DeliveryEventProducer producer) {
        this.producer = producer;
    }

    public void assignDelivery(Long orderId) {

        log.info("Assigning delivery agent for orderId={}", orderId);

        producer.sendDeliveryUpdate(orderId, "OUT_FOR_DELIVERY");

        log.info("Order {} is OUT_FOR_DELIVERY", orderId);

        completeDelivery(orderId);
    }

    @Async
    public void completeDelivery(Long orderId) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Order {} delivered successfully", orderId);
        producer.sendDeliveryUpdate(orderId, "DELIVERED");
    }
}
