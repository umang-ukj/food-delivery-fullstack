package com.example.delivery.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.delivery.producer.DeliveryEventProducer;
import com.example.delivery.repository.DeliveryRepository;
import com.fd.events.DeliveryEvent;
import com.fd.events.DeliveryStatus;

@Service
public class DeliveryAsyncService {

    private final DeliveryEventProducer deliveryProducer;
    private final DeliveryRepository deliveryRepository;

    public DeliveryAsyncService(DeliveryEventProducer deliveryProducer, DeliveryRepository deliveryRepository) {
        this.deliveryProducer = deliveryProducer;
		this.deliveryRepository = deliveryRepository;
    }

    @Async
    public void runDeliveryFlow(Long orderId) {
        try {
        	Thread.sleep(3000);
            if (isCancelled(orderId)) return;
            deliveryProducer.publish(new DeliveryEvent(orderId, DeliveryStatus.PICKED_UP));

            Thread.sleep(3000);
            if (isCancelled(orderId)) return;
            deliveryProducer.publish(new DeliveryEvent(orderId, DeliveryStatus.OUT_FOR_DELIVERY));

            Thread.sleep(3000);
            if (isCancelled(orderId)) return;
            deliveryProducer.publish(new DeliveryEvent(orderId, DeliveryStatus.DELIVERED));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private boolean isCancelled(Long orderId) {
        return deliveryRepository.findFirstByOrderId(orderId)
                .map(delivery -> delivery.getStatus() == DeliveryStatus.CANCELLED)
                .orElse(false);
    }
}

