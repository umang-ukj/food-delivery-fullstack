package com.example.delivery.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.delivery.model.Delivery;
import com.example.delivery.repository.DeliveryRepository;
import com.fd.events.DeliveryEvent;


//sends delivery update to delivery events topic of kafka 
@Component
public class DeliveryEventProducer {

    
    public DeliveryEventProducer(DeliveryRepository deliveryRepository,
			KafkaTemplate<String, DeliveryEvent> kafkaTemplate) {
		super();
		this.deliveryRepository = deliveryRepository;
		this.kafkaTemplate = kafkaTemplate;
	}

	private final DeliveryRepository deliveryRepository;

	private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

    public void sendDeliveryUpdate(Long orderId, String status) {
    	Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setStatus(status);
        deliveryRepository.save(delivery);
        
        DeliveryEvent event = new DeliveryEvent(orderId, status);
        kafkaTemplate.send(
        	    "delivery-events",
        	    new DeliveryEvent(orderId, status)
        	);

    }
}
