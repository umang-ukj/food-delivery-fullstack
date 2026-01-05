package com.example.delivery.producer;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.delivery.model.Delivery;
import com.example.delivery.model.DeliveryStatus;
import com.example.delivery.repository.DeliveryRepository;
import com.fd.events.DeliveryEvent;


//sends delivery update to delivery events topic of kafka 
@Component
public class DeliveryEventProducer {

	private static final Logger log =LoggerFactory.getLogger(DeliveryEventProducer.class);

    public DeliveryEventProducer(DeliveryRepository deliveryRepository,
			KafkaTemplate<String, DeliveryEvent> kafkaTemplate) {
		super();
		this.deliveryRepository = deliveryRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	private final DeliveryRepository deliveryRepository;

	private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

    public void sendDeliveryUpdate(Long orderId, DeliveryStatus status) {
    	Optional<Delivery> existing = deliveryRepository.findByOrderId(orderId);

        if (existing.isPresent()) {
            log.info("Delivery already processed for orderId={}, skipping", orderId);
            return;
        }

        log.info("Processing delivery for orderId={}", orderId);
    	Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setStatus(status);
        deliveryRepository.save(delivery);
        log.info("Publishing DELIVERY_{} event for orderId={}", status, orderId);

        DeliveryEvent event = new DeliveryEvent(orderId, status.name());
        kafkaTemplate.send(
        	    "delivery-events",
        	    event
        	);

    }
}
