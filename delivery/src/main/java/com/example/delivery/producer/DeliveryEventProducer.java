package com.example.delivery.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.example.delivery.model.DeliveryEvent;

//sends delivery update to delivery events topic of kafka 
@Component
public class DeliveryEventProducer {

    public DeliveryEventProducer(KafkaTemplate<String, DeliveryEvent> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}

	private final KafkaTemplate<String, DeliveryEvent> kafkaTemplate;

    public void sendDeliveryUpdate(Long orderId, String status) {
        DeliveryEvent event = new DeliveryEvent(orderId, status);
        kafkaTemplate.send("delivery-events", event);
    }
}
