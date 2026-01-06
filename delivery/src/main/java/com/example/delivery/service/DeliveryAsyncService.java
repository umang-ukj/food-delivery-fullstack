package com.example.delivery.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.delivery.producer.DeliveryEventProducer;
import com.fd.events.DeliveryEvent;
import com.fd.events.DeliveryStatus;

@Service
public class DeliveryAsyncService {

    private final DeliveryEventProducer deliveryProducer;

    public DeliveryAsyncService(DeliveryEventProducer deliveryProducer) {
        this.deliveryProducer = deliveryProducer;
    }

    @Async
    public void runDeliveryFlow(Long orderId) {
        try {
            Thread.sleep(1000);
            deliveryProducer.publish(new DeliveryEvent(orderId, DeliveryStatus.PICKED_UP));

            Thread.sleep(1000);
            deliveryProducer.publish(new DeliveryEvent(orderId, DeliveryStatus.OUT_FOR_DELIVERY));

            Thread.sleep(1000);
            deliveryProducer.publish(new DeliveryEvent(orderId, DeliveryStatus.DELIVERED));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

