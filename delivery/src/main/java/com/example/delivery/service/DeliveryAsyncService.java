package com.example.delivery.service;

import java.util.Map;

import org.slf4j.MDC;
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
    public void runDeliveryFlow(Long orderId, String traceId) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        if (traceId != null && !traceId.isBlank()) {
            MDC.put("traceId", traceId);
        } else {
            MDC.remove("traceId");
        }
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
        }finally {
            if (previousContext != null) {
                MDC.setContextMap(previousContext);
            } else {
                MDC.clear();
            }}
    }
    
    private boolean isCancelled(Long orderId) {
        return deliveryRepository.findFirstByOrderId(orderId)
                .map(delivery -> delivery.getStatus() == DeliveryStatus.CANCELLED)
                .orElse(false);
    }
}

