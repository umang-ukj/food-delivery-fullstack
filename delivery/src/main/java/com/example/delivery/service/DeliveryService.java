package com.example.delivery.service;

import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.delivery.model.Delivery;
import com.example.delivery.producer.DeliveryEventProducer;
import com.example.delivery.repository.DeliveryRepository;
import com.fd.events.DeliveryEvent;
import com.fd.events.DeliveryStatus;

import jakarta.transaction.Transactional;


@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryEventProducer deliveryProducer;
    private final DeliveryAsyncService asyncService;

    public DeliveryService(DeliveryRepository deliveryRepository,DeliveryEventProducer deliveryProducer,DeliveryAsyncService asyncService)
    {
        this.deliveryRepository = deliveryRepository;
        this.deliveryProducer = deliveryProducer;
        this.asyncService=asyncService;
    }

    @Transactional
    public void createDelivery(Long orderId) {
    	Optional<Delivery> existing = deliveryRepository.findFirstByOrderId(orderId);
        // 1️Idempotency check (VERY IMPORTANT)
        if (existing.isPresent()) {
            return;
        }

        // 2️ Create delivery
        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setStatus(DeliveryStatus.CREATED);
        

        deliveryRepository.save(delivery);

        // 3️ Publish CREATED
        publishStatus(orderId, DeliveryStatus.CREATED);
        asyncService.runDeliveryFlow(orderId);
    }
    
    

    @Transactional
    protected void update(Delivery delivery, DeliveryStatus status) {
        delivery.setStatus(status);
        deliveryRepository.save(delivery);
        publishStatus(delivery.getOrderId(), status);
    }

    private void publishStatus(Long orderId, DeliveryStatus status) {
        deliveryProducer.publish(new DeliveryEvent(orderId, status));
    }
    
    @Transactional
    public void update(Long orderId, DeliveryStatus status) {

        Delivery delivery = deliveryRepository.findFirstByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException(
                        "Delivery not found for orderId=" + orderId));

        delivery.setStatus(status);
        deliveryRepository.save(delivery);

        publishStatus(orderId, status);
    }

}
